package com.hiking.treasure.tools;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * MySQL 8.0 -> MyBatis 代码生成器（构造器注入风格）
 * 生成：entity / mapper / mapper.xml / service / service.impl / controller
 * 选择表：优先使用 INCLUDE_TABLES；为空则运行时交互选择（序号 / 表名 / all）
 */
public class CodeGenerator {

    /* ===================== 数据库配置（按需修改） ===================== */
    private static final String JDBC_URL = "jdbc:mysql://localhost:3306/admin_template"
            + "?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false";
    private static final String JDBC_USER = "root";
    private static final String JDBC_PWD  = "aA123456";
    private static final String DB_NAME   = "admin_template";

    /* ===================== 包 & 目录（按需修改） ===================== */
    private static final String BASE_PACKAGE = "com.hiking.treasure";
    private static final String MODULE       = "system"; // 例：system/treasure/auth...
    private static final String OUTPUT_JAVA  = "src/main/java/";
    private static final String OUTPUT_RES   = "src/main/resources/";

    /* 生成类名时移除的表前缀（命中第一个即移除） */
    private static final String[] TABLE_PREFIX = {"sys_", "t_"};

    /* Swagger 注解开关（需 springdoc-openapi 依赖）；PageHelper 示例开关 */
    private static final boolean SWAGGER = true;
    private static final boolean USE_PAGEHELPER = false; // 需要时置 true 并添加依赖

    /* 指定要生成的表（为空则交互选择） */
    private static final String[] INCLUDE_TABLES = {
            // "sys_user", "sys_role"
    };

    /* ===================== MySQL -> Java 类型映射 ===================== */
    private static final Map<String, String> MYSQL_TYPE_2_JAVA = new HashMap<>();
    static {
        MYSQL_TYPE_2_JAVA.put("varchar", "String");
        MYSQL_TYPE_2_JAVA.put("char", "String");
        MYSQL_TYPE_2_JAVA.put("text", "String");
        MYSQL_TYPE_2_JAVA.put("longtext", "String");
        MYSQL_TYPE_2_JAVA.put("mediumtext", "String");
        MYSQL_TYPE_2_JAVA.put("json", "String");

        MYSQL_TYPE_2_JAVA.put("bigint", "Long");
        MYSQL_TYPE_2_JAVA.put("int", "Integer");
        MYSQL_TYPE_2_JAVA.put("integer", "Integer");
        MYSQL_TYPE_2_JAVA.put("tinyint", "Integer"); // 如需 Boolean 可定制 tinyint(1) -> Boolean
        MYSQL_TYPE_2_JAVA.put("smallint", "Integer");
        MYSQL_TYPE_2_JAVA.put("mediumint", "Integer");

        MYSQL_TYPE_2_JAVA.put("bit", "Boolean");
        MYSQL_TYPE_2_JAVA.put("boolean", "Boolean");

        MYSQL_TYPE_2_JAVA.put("decimal", "BigDecimal");
        MYSQL_TYPE_2_JAVA.put("numeric", "BigDecimal");
        MYSQL_TYPE_2_JAVA.put("double", "Double");
        MYSQL_TYPE_2_JAVA.put("float", "Float");

        MYSQL_TYPE_2_JAVA.put("date", "LocalDate");
        MYSQL_TYPE_2_JAVA.put("datetime", "LocalDateTime");
        MYSQL_TYPE_2_JAVA.put("timestamp", "LocalDateTime");
        MYSQL_TYPE_2_JAVA.put("time", "String");
        MYSQL_TYPE_2_JAVA.put("year", "Integer");
    }

    public static void main(String[] args) throws Exception {
        try (Connection conn = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PWD)) {
            List<TableMeta> all = queryAllTables(conn, DB_NAME);
            if (all.isEmpty()) {
                System.out.println("❗ 未找到任何表，请检查 DB_NAME / 连接信息。");
                return;
            }
            List<TableMeta> targets = pickTables(all);
            for (TableMeta t : targets) {
                t.columns = queryColumns(conn, DB_NAME, t.tableName);
                generateAll(t);
            }
            System.out.println("✅ 生成完成，共生成表：" + targets.size());
        }
    }

    /* ===================== 表选择：常量优先，交互兜底 ===================== */
    private static List<TableMeta> pickTables(List<TableMeta> all) {
        if (INCLUDE_TABLES != null && INCLUDE_TABLES.length > 0) {
            Set<String> inc = Arrays.stream(INCLUDE_TABLES)
                    .filter(s -> s != null && !s.isBlank())
                    .map(String::toLowerCase)
                    .collect(Collectors.toCollection(LinkedHashSet::new));
            List<TableMeta> fixed = all.stream()
                    .filter(t -> inc.contains(t.tableName.toLowerCase()))
                    .collect(Collectors.toList());
            if (!fixed.isEmpty()) {
                System.out.println("使用 INCLUDE_TABLES：" + inc);
                return fixed;
            }
            System.out.println("INCLUDE_TABLES 未匹配到表，转为交互选择...");
        }
        return selectTablesInteractively(all);
    }

    private static List<TableMeta> selectTablesInteractively(List<TableMeta> all) {
        System.out.println("\n=== 数据库表 ===");
        for (int i = 0; i < all.size(); i++) {
            System.out.printf("%2d) %-40s %s%n", i + 1, all.get(i).tableName, nv(all.get(i).comment));
        }
        System.out.println("\n输入要生成的表：");
        System.out.println("- 输入序号（如：1,3,8）或表名（如：sys_user,sys_role）");
        System.out.println("- 输入 all 生成全部；直接回车默认 all：");

        Scanner sc = new Scanner(System.in);
        String line = sc.nextLine().trim();
        if (line.isEmpty() || "all".equalsIgnoreCase(line)) return all;

        Set<String> selected = new LinkedHashSet<>();
        for (String part : line.split(",")) {
            String p = part.trim();
            if (p.isEmpty()) continue;
            if (p.matches("\\d+")) {
                int idx = Integer.parseInt(p);
                if (idx >= 1 && idx <= all.size()) selected.add(all.get(idx - 1).tableName.toLowerCase());
            } else {
                selected.add(p.toLowerCase());
            }
        }
        List<TableMeta> res = all.stream().filter(t -> selected.contains(t.tableName.toLowerCase())).collect(Collectors.toList());
        if (res.isEmpty()) {
            System.out.println("未匹配到任何表，默认生成全部。");
            return all;
        }
        return res;
    }

    /* ===================== information_schema 查询 ===================== */
    private static List<TableMeta> queryAllTables(Connection conn, String dbName) throws SQLException {
        String sql = """
            SELECT TABLE_NAME, IFNULL(TABLE_COMMENT,'') AS TABLE_COMMENT
            FROM information_schema.TABLES
            WHERE TABLE_SCHEMA = ? AND TABLE_TYPE = 'BASE TABLE'
            ORDER BY TABLE_NAME
        """;
        List<TableMeta> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, dbName);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String name = rs.getString("TABLE_NAME");
                    String comment = rs.getString("TABLE_COMMENT");
                    String lower = name.toLowerCase();
                    if (lower.startsWith("flyway_") || lower.startsWith("qrtz_")) continue; // 忽略系统表
                    TableMeta t = new TableMeta();
                    t.tableName = name;
                    t.comment = comment == null ? "" : comment;
                    list.add(t);
                }
            }
        }
        return list;
    }

    private static List<ColumnMeta> queryColumns(Connection conn, String dbName, String table) throws SQLException {
        String colSql = """
            SELECT COLUMN_NAME, DATA_TYPE, CHARACTER_MAXIMUM_LENGTH, NUMERIC_SCALE,
                   IFNULL(COLUMN_COMMENT,'') AS REMARKS
            FROM information_schema.COLUMNS
            WHERE TABLE_SCHEMA = ? AND TABLE_NAME = ?
            ORDER BY ORDINAL_POSITION
        """;
        String pkSql = """
            SELECT k.COLUMN_NAME
            FROM information_schema.TABLE_CONSTRAINTS t
            JOIN information_schema.KEY_COLUMN_USAGE k
              ON t.CONSTRAINT_NAME = k.CONSTRAINT_NAME
             AND t.TABLE_SCHEMA = k.TABLE_SCHEMA
             AND t.TABLE_NAME = k.TABLE_NAME
            WHERE t.TABLE_SCHEMA = ? AND t.TABLE_NAME = ? AND t.CONSTRAINT_TYPE = 'PRIMARY KEY'
        """;

        List<ColumnMeta> cols = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(colSql)) {
            ps.setString(1, dbName);
            ps.setString(2, table);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ColumnMeta c = new ColumnMeta();
                    c.columnName    = rs.getString("COLUMN_NAME");
                    c.typeName      = rs.getString("DATA_TYPE"); // e.g. varchar/int/datetime
                    c.columnSize    = optNum(rs.getObject("CHARACTER_MAXIMUM_LENGTH"));
                    c.decimalDigits = optNum(rs.getObject("NUMERIC_SCALE"));
                    c.remarks       = rs.getString("REMARKS");
                    cols.add(c);
                }
            }
        }
        // PK
        Set<String> pkCols = new HashSet<>();
        try (PreparedStatement ps = conn.prepareStatement(pkSql)) {
            ps.setString(1, dbName);
            ps.setString(2, table);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) pkCols.add(rs.getString(1));
            }
        }
        for (ColumnMeta c : cols) c.primaryKey = pkCols.contains(c.columnName);
        return cols;
    }

    /* ===================== 代码生成入口 ===================== */
    private static void generateAll(TableMeta t) throws IOException {
        String className = toClassName(t.tableName);
        String entityPackage      = BASE_PACKAGE + ".modules." + MODULE + ".entity";
        String mapperPackage      = BASE_PACKAGE + ".modules." + MODULE + ".mapper";
        String servicePackage     = BASE_PACKAGE + ".modules." + MODULE + ".service";
        String serviceImplPackage = servicePackage + ".impl";
        String controllerPackage  = BASE_PACKAGE + ".modules." + MODULE + ".controller";

        String entityDir      = OUTPUT_JAVA + pkg2path(entityPackage);
        String mapperDir      = OUTPUT_JAVA + pkg2path(mapperPackage);
        String serviceDir     = OUTPUT_JAVA + pkg2path(servicePackage);
        String serviceImplDir = OUTPUT_JAVA + pkg2path(serviceImplPackage);
        String controllerDir  = OUTPUT_JAVA + pkg2path(controllerPackage);
        String xmlDir         = OUTPUT_RES  + "mapper/" + MODULE;

        mkdirs(entityDir, mapperDir, serviceDir, serviceImplDir, controllerDir, xmlDir);

        write(entityDir + "/" + className + ".java", renderEntity(t, className, entityPackage));
        write(mapperDir + "/" + className + "Mapper.java", renderMapperInterface(className, mapperPackage, entityPackage));
        write(xmlDir + "/" + className + "Mapper.xml", renderMapperXml(t, className, mapperPackage, entityPackage));
        write(serviceDir + "/" + className + "Service.java", renderServiceInterface(className, servicePackage, entityPackage));
        write(serviceImplDir + "/" + className + "ServiceImpl.java",
                renderServiceImpl(t, className, serviceImplPackage, servicePackage, mapperPackage, entityPackage));
        write(controllerDir + "/" + className + "Controller.java",
                renderController(className, controllerPackage, servicePackage, entityPackage));
    }

    /* ===================== 各模板 ===================== */
    private static String renderEntity(TableMeta t, String className, String pkg) {
        StringBuilder sb = new StringBuilder();
        Set<String> imports = new TreeSet<>();
        imports.add("lombok.Data");
        if (SWAGGER) imports.add("io.swagger.v3.oas.annotations.media.Schema");
        for (ColumnMeta c : t.columns) {
            String jt = toJavaType(c.typeName);
            if ("BigDecimal".equals(jt))    imports.add(BigDecimal.class.getName());
            if ("LocalDateTime".equals(jt)) imports.add(LocalDateTime.class.getName());
            if ("LocalDate".equals(jt))     imports.add(LocalDate.class.getName());
        }

        sb.append("package ").append(pkg).append(";\n\n");
        for (String im : imports) sb.append("import ").append(im).append(";\n");
        sb.append("\n");
        if (SWAGGER) sb.append("@Schema(name = \"").append(className).append("\")\n");
        sb.append("@Data\n");
        sb.append("public class ").append(className).append(" implements java.io.Serializable {\n\n");
        for (ColumnMeta c : t.columns) {
            String field = toCamel(c.columnName);
            String type = toJavaType(c.typeName);
            String comment = c.remarks == null ? "" : c.remarks.replace("\"", "'");
            if (SWAGGER) sb.append("    @Schema(description = \"").append(comment).append("\")\n");
            sb.append("    private ").append(type).append(" ").append(field).append(";\n\n");
        }
        sb.append("}\n");
        return sb.toString();
    }

    private static String renderMapperInterface(String className, String mapperPkg, String entityPkg) {
        return """
               package %s;
               
               import org.apache.ibatis.annotations.Mapper;
               import %s.%s;
               import java.util.List;
               
               @Mapper
               public interface %sMapper {
                   %s selectById(Long id);
                   List<%s> selectList(%s query);
                   int insert(%s entity);
                   int updateById(%s entity);
                   int deleteById(Long id);
               }
               """.formatted(
                mapperPkg,
                entityPkg, className,
                className,
                className,
                className, className,
                className,
                className
        );
    }

    private static String renderMapperXml(TableMeta t, String className, String mapperPkg, String entityPkg) {
        String table = t.tableName;
        String type = entityPkg + "." + className;
        String idCol = t.columns.stream().filter(c -> c.primaryKey).map(c -> c.columnName).findFirst().orElse("id");

        String resultMappings = buildResultMappings(t);
        String allCols   = t.columns.stream().map(c -> "`" + c.columnName + "`").collect(Collectors.joining(", "));
        String insertCols = allCols;
        String insertVals = t.columns.stream().map(c -> "#{" + toCamel(c.columnName) + "}").collect(Collectors.joining(", "));

        String setClause = t.columns.stream()
                .filter(c -> !c.columnName.equalsIgnoreCase(idCol))
                .map(c -> "            <if test=\"" + toCamel(c.columnName) + "!=null\">`" + c.columnName + "`=#{" + toCamel(c.columnName) + "},</if>")
                .collect(Collectors.joining("\n"));

        String whereAnd = t.columns.stream()
                .map(c -> "            <if test=\"" + toCamel(c.columnName) + "!=null\">AND `" + c.columnName + "` = #{" + toCamel(c.columnName) + "}</if>")
                .collect(Collectors.joining("\n"));

        return """
               <?xml version="1.0" encoding="UTF-8"?>
               <!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
                       "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
               <mapper namespace="%s.%sMapper">
               
                   <resultMap id="BaseResultMap" type="%s">
               %s
                   </resultMap>
               
                   <sql id="Base_Column_List">
                       %s
                   </sql>
               
                   <select id="selectById" resultMap="BaseResultMap">
                       SELECT
                       <include refid="Base_Column_List"/>
                       FROM `%s`
                       WHERE `%s` = #{id}
                   </select>
               
                   <select id="selectList" resultMap="BaseResultMap">
                       SELECT
                       <include refid="Base_Column_List"/>
                       FROM `%s`
                       <where>
               %s
                       </where>
                   </select>
               
                   <insert id="insert" parameterType="%s">
                       INSERT INTO `%s` (%s)
                       VALUES (%s)
                   </insert>
               
                   <update id="updateById" parameterType="%s">
                       UPDATE `%s`
                       <set>
               %s
                       </set>
                       WHERE `%s` = #{%s}
                   </update>
               
                   <delete id="deleteById">
                       DELETE FROM `%s` WHERE `%s` = #{id}
                   </delete>
               </mapper>
               """.formatted(
                mapperPkg, className,
                type,
                resultMappings,
                allCols,
                table,
                idCol,
                table,
                whereAnd,
                type,
                table, insertCols, insertVals,
                type,
                table,
                setClause,
                idCol, toCamel(idCol),
                table, idCol
        );
    }

    private static String buildResultMappings(TableMeta t) {
        StringBuilder sb = new StringBuilder();
        for (ColumnMeta c : t.columns) {
            String prop = toCamel(c.columnName);
            String jdbcType = toJdbcType(c.typeName);
            String tag = c.primaryKey ? "id" : "result";
            sb.append("        <").append(tag)
                    .append(" column=\"").append(c.columnName).append("\"")
                    .append(" property=\"").append(prop).append("\"");
            if (jdbcType != null) sb.append(" jdbcType=\"").append(jdbcType).append("\"");
            sb.append("/>\n");
        }
        return sb.toString();
    }

    private static String renderServiceInterface(String className, String servicePkg, String entityPkg) {
        String entity = entityPkg + "." + className;
        return """
               package %s;
               
               import %s;
               import java.util.List;
               
               public interface %sService {
                   %s findById(Long id);
                   List<%s> list(%s query, Integer pageNum, Integer pageSize);
                   Long create(%s entity);
                   boolean update(%s entity);
                   boolean remove(Long id);
               }
               """.formatted(servicePkg, entity, className, className, className, className, className, className);
    }

    private static String renderServiceImpl(TableMeta t, String className, String implPkg, String svcPkg, String mapperPkg, String entityPkg) {
        String idField = t.columns.stream().filter(c -> c.primaryKey).map(c -> toCamel(c.columnName)).findFirst().orElse("id");

        String pageHelperImport = USE_PAGEHELPER ? "import com.github.pagehelper.PageHelper;\n" : "";
        String pageHelperStart  = USE_PAGEHELPER ? "        if (pageNum != null && pageSize != null) { PageHelper.startPage(pageNum, pageSize); }\n" : "";

        return """
               package %s;
               
               import %s.%sService;
               import %s.%sMapper;
               import %s.%s;
               import org.springframework.stereotype.Service;
               import java.util.List;
               
               @Service
               public class %sServiceImpl implements %sService {
               
                   private final %s mapper;
               
                   public %sServiceImpl(%s mapper) {
                       this.mapper = mapper;
                   }
               
                   @Override
                   public %s findById(Long id) {
                       return mapper.selectById(id);
                   }
               
                   @Override
                   public List<%s> list(%s query, Integer pageNum, Integer pageSize) {
               %s        return mapper.selectList(query);
                   }
               
                   @Override
                   public Long create(%s entity) {
                       mapper.insert(entity);
                       try {
                           var f = entity.getClass().getDeclaredField("%s");
                           f.setAccessible(true);
                           Object v = f.get(entity);
                           return v == null ? null : Long.valueOf(String.valueOf(v));
                       } catch (Exception ignore) { return null; }
                   }
               
                   @Override
                   public boolean update(%s entity) {
                       return mapper.updateById(entity) > 0;
                   }
               
                   @Override
                   public boolean remove(Long id) {
                       return mapper.deleteById(id) > 0;
                   }
               }
               """.formatted(
                implPkg,
                svcPkg, className,
                mapperPkg, className,
                entityPkg, className,
                className, className,
                className + "Mapper",
                className, className + "Mapper",
                className,
                className, className,
                pageHelperImport + pageHelperStart,
                className,
                idField,
                className
        );
    }

    private static String renderController(String className, String controllerPkg, String servicePkg, String entityPkg) {
        String swaggerImport  = SWAGGER ? """
            import io.swagger.v3.oas.annotations.Operation;
            import io.swagger.v3.oas.annotations.tags.Tag;
            """ : "";
        String swaggerClassAnno = SWAGGER ? "@Tag(name = \"" + className + " 管理\")\n" : "";
        String op = SWAGGER ? "@Operation" : "";

        return """
               package %s;
               
               import %s.%sService;
               import %s.%s;
               import %s.common.api.ApiResponse;
               import org.springframework.web.bind.annotation.*;
               %s
               import java.util.List;
               
               %s
               @RestController
               @RequestMapping("/api/%s")
               public class %sController {
               
                   private final %sService service;
               
                   public %sController(%sService service) {
                       this.service = service;
                   }
               
                   %s(summary = "详情")
                   @GetMapping("/{id}")
                   public ApiResponse<%s> detail(@PathVariable Long id) {
                       return ApiResponse.success(service.findById(id));
                   }
               
                   %s(summary = "列表")
                   @GetMapping("/list")
                   public ApiResponse<List<%s>> list(%s query,
                                                     @RequestParam(required = false) Integer pageNum,
                                                     @RequestParam(required = false) Integer pageSize) {
                       return ApiResponse.success(service.list(query, pageNum, pageSize));
                   }
               
                   %s(summary = "新增")
                   @PostMapping
                   public ApiResponse<Long> create(@RequestBody %s req) {
                       return ApiResponse.success(service.create(req));
                   }
               
                   %s(summary = "更新")
                   @PutMapping
                   public ApiResponse<Boolean> update(@RequestBody %s req) {
                       return ApiResponse.success(service.update(req));
                   }
               
                   %s(summary = "删除")
                   @DeleteMapping("/{id}")
                   public ApiResponse<Boolean> remove(@PathVariable Long id) {
                       return ApiResponse.success(service.remove(id));
                   }
               }
               """.formatted(
                controllerPkg,
                servicePkg, className,
                entityPkg, className,
                BASE_PACKAGE,
                swaggerImport,
                swaggerClassAnno,
                toKebab(className),
                className,
                className,
                className, className,
                op, className,
                op, className, className,
                op, className,
                op, className,
                op
        );
    }

    /* ===================== 工具 & 内部模型 ===================== */
    private static void write(String path, String content) throws IOException {
        File f = new File(path);
        f.getParentFile().mkdirs();
        try (FileOutputStream fos = new FileOutputStream(f);
             OutputStreamWriter osw = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
             BufferedWriter bw = new BufferedWriter(osw)) {
            bw.write(content);
        }
        System.out.println(" - write: " + path);
    }

    private static void mkdirs(String... dirs) {
        for (String d : dirs) {
            File f = new File(d);
            if (!f.exists()) f.mkdirs();
        }
    }

    private static String pkg2path(String pkg) { return pkg.replace('.', '/'); }

    private static String toClassName(String table) {
        String name = table;
        for (String p : TABLE_PREFIX) {
            if (name.startsWith(p)) { name = name.substring(p.length()); break; }
        }
        StringBuilder sb = new StringBuilder();
        for (String part : name.split("_")) {
            if (part.isBlank()) continue;
            sb.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1).toLowerCase());
        }
        return sb.toString();
    }

    private static String toCamel(String col) {
        String[] parts = col.split("_");
        StringBuilder sb = new StringBuilder(parts[0].toLowerCase());
        for (int i = 1; i < parts.length; i++) {
            String s = parts[i].toLowerCase();
            sb.append(Character.toUpperCase(s.charAt(0))).append(s.substring(1));
        }
        return sb.toString();
    }

    private static String toKebab(String s) {
        StringBuilder out = new StringBuilder();
        for (char c : s.toCharArray()) {
            if (Character.isUpperCase(c)) {
                if (out.length() > 0) out.append("-");
                out.append(Character.toLowerCase(c));
            } else {
                out.append(c);
            }
        }
        return out.toString();
    }

    private static String toJavaType(String mysqlType) {
        return MYSQL_TYPE_2_JAVA.getOrDefault(mysqlType.toLowerCase(), "String");
    }

    private static String toJdbcType(String mysqlType) {
        String t = mysqlType.toUpperCase();
        Set<String> known = new HashSet<>(Arrays.asList(
                "VARCHAR","CHAR","TEXT","LONGTEXT","MEDIUMTEXT","JSON",
                "BIGINT","INT","INTEGER","TINYINT","SMALLINT","MEDIUMINT",
                "BIT","BOOLEAN","DECIMAL","NUMERIC","DOUBLE","FLOAT",
                "DATE","DATETIME","TIMESTAMP","TIME","YEAR"
        ));
        if (known.contains(t)) return t;
        if ("MEDIUMINT".equals(t)) return "INTEGER";
        return null;
    }

    private static int optNum(Object obj) {
        if (obj == null) return 0;
        if (obj instanceof Number) return ((Number) obj).intValue();
        try { return Integer.parseInt(obj.toString()); } catch (NumberFormatException e) { return 0; }
    }

    private static String nv(String s) { return s == null ? "" : s; }

    /* ---- 内部模型 ---- */
    private static class TableMeta {
        String tableName;
        String comment;
        List<ColumnMeta> columns = new ArrayList<>();
    }
    private static class ColumnMeta {
        String columnName;
        String typeName;       // DATA_TYPE (小写)
        int columnSize;
        int decimalDigits;
        String remarks;
        boolean primaryKey;
    }
}
