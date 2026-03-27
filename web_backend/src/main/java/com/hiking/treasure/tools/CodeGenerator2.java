package com.hiking.treasure.tools;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.OutputFile;
import com.baomidou.mybatisplus.generator.config.rules.DateType;
import com.baomidou.mybatisplus.generator.config.rules.DbColumnType;
import com.baomidou.mybatisplus.generator.engine.VelocityTemplateEngine;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CodeGenerator2 {
    public static void main(String[] args) {
        // === 1. 基础信息 ===
        String url = "jdbc:mysql://localhost:3306/admin_template?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai";
        String username = "root";
        String password = "aA123456";

        // 生成到当前工程的相对路径
        String projectPath = System.getProperty("user.dir" );
        String javaOut = projectPath + "/src/main/java";
        String xmlOut = projectPath + "/src/main/resources/mapper";

        // 需要生成的表（可改成从命令行或配置读取）
        List<String> includeTables = List.of(
                "sys_tenant" , "sys_user" , "sys_role" , "sys_permission" , "sys_dict" , "sys_dict_item" , "sys_announcement" , "sys_file"
        );

        Map<String, Object> cfg = new HashMap<>();
        cfg.put("baseRequestMapping" , "/api" ); // 任选
        cfg.put("resultClass" , "com.hiking.treasure.common.api.vo.Result" );
        cfg.put("resultSimpleName" , "Result" );
        cfg.put("baseControllerPackage" , "com.hiking.treasure.common.web" );
        cfg.put("baseControllerClass" , "com.hiking.treasure.common.web.BaseController" );
        cfg.put("voPackage" , "com.hiking.treasure.domain.vo" );
        cfg.put("dtoPackage" , "com.hiking.treasure.domain.dto" );
        cfg.put("convertPackage" , "com.hiking.treasure.domain.convert" );


        FastAutoGenerator.create(url, username, password)
                .globalConfig(builder -> builder
                        .author("hx" )              // 作者
                        .dateType(DateType.TIME_PACK)
                        .commentDate("yyyy-MM-dd" )
                        .outputDir(javaOut)
                        .disableOpenDir()
                )
                .dataSourceConfig(builder -> builder.typeConvertHandler(
                        (global, typeRegistry, metaInfo) -> {
                            if ("TINYINT".equalsIgnoreCase(metaInfo.getTypeName())) {
                                return DbColumnType.INTEGER;
                            }
                            if ("BIT".equalsIgnoreCase(metaInfo.getTypeName()) && Integer.valueOf(1).equals(metaInfo.getLength())) {
                                return DbColumnType.INTEGER;
                            }
                            return typeRegistry.getColumnType(metaInfo);
                        }
                ))
                .packageConfig(builder -> builder
                        .parent("com.hiking.treasure" ) // 顶级包
                        .entity("entity" )
                        .service("service" )
                        .serviceImpl("service.impl" )
                        .mapper("mapper" )
                        .xml("mapper" )
                        .controller("controller" )
                        .pathInfo(Collections.singletonMap(OutputFile.xml, xmlOut)) // 指定 XML 生成目录
                )
                .injectionConfig(c ->
                        c.customMap(cfg)
                                .customFile(fileBuilder -> fileBuilder.fileName("CreateDTO.java" )
                                        .packageName("domain.dto.create" )
                                        .templatePath("/templates/mybatis-plus/dto-create.java.vm" ).build())
                                .customFile(fileBuilder -> fileBuilder.fileName("UpdateDTO.java" )
                                        .packageName("domain.dto.update" )
                                        .templatePath("/templates/mybatis-plus/dto-update.java.vm" ).build())
                                .customFile(fileBuilder -> fileBuilder.fileName("QueryDTO.java" )
                                        .packageName("domain.dto.query" )
                                        .templatePath("/templates/mybatis-plus/dto-query.java.vm" ).build())
                                .customFile(fileBuilder -> fileBuilder.fileName("VO.java" )
                                        .packageName("domain.vo" )
                                        .templatePath("/templates/mybatis-plus/vo.java.vm" ).build())
                                .customFile(fileBuilder -> fileBuilder.fileName("Convert.java" )
                                        .packageName("domain.convert" )
                                        .templatePath("/templates/mybatis-plus/convert.java.vm" ).build()))
                .strategyConfig(builder -> builder
                        .addInclude(includeTables)
                        .addTablePrefix("t_" , "sys_" ) // 去除前缀（按需）
                        // Entity
                        .entityBuilder()
                        .enableLombok()
                        .formatFileName("%s" )
                        .enableChainModel()
                        .enableTableFieldAnnotation()              // 生成 @TableField/@TableId
                        .enableSerialAnnotation()
                        .idType(IdType.ASSIGN_ID)                  // 雪花ID，varchar 存储
                        .logicDeleteColumnName("del_flag" )         // 数据库列名
                        .logicDeletePropertyName("delFlag" )        // 实体属性名（驼峰）
                        .addTableFills(
                                new com.baomidou.mybatisplus.generator.fill.Column("create_time" ,
                                        com.baomidou.mybatisplus.annotation.FieldFill.INSERT),
                                new com.baomidou.mybatisplus.generator.fill.Column("update_time" ,
                                        com.baomidou.mybatisplus.annotation.FieldFill.INSERT_UPDATE),
                                new com.baomidou.mybatisplus.generator.fill.Column("create_by" ,
                                        com.baomidou.mybatisplus.annotation.FieldFill.INSERT),
                                new com.baomidou.mybatisplus.generator.fill.Column("update_by" ,
                                        com.baomidou.mybatisplus.annotation.FieldFill.INSERT_UPDATE),
                                new com.baomidou.mybatisplus.generator.fill.Column("del_flag" ,
                                        com.baomidou.mybatisplus.annotation.FieldFill.INSERT)
                        )
                        // Mapper
                        .mapperBuilder()
                        .enableBaseResultMap()
                        .enableBaseColumnList()
                        .formatMapperFileName("%sMapper" )
                        .formatXmlFileName("%sMapper" )
                        .enableFileOverride()
                        // Service
                        .serviceBuilder()
                        .formatServiceFileName("%sService" )
                        .formatServiceImplFileName("%sServiceImpl" )
                        .enableFileOverride()
                        // Controller
                        .controllerBuilder()
                        .enableRestStyle()
                        .template("/templates/mybatis-plus/controller.java.vm" )
                        .formatFileName("%sController" )
                        .enableFileOverride()
                        .enableRestStyle()
                )
                .templateEngine(new VelocityTemplateEngine()) // 模板引擎
                .execute();

        System.out.println("MyBatis-Plus 代码生成完成！" );
    }
}
