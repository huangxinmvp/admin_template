import type { GenericRecord, OptionItem, TreeOption } from '@/services/backend/types';

export type BackendFieldType =
  | 'text'
  | 'textarea'
  | 'number'
  | 'date'
  | 'datetime'
  | 'select'
  | 'multiselect'
  | 'treeSelect'
  | 'treeMultiselect'
  | 'password'
  | 'image'
  | 'file';

export interface BackendFieldConfig {
  name: string;
  label: string;
  type?: BackendFieldType;
  hideInTable?: boolean;
  hideInForm?: boolean;
  hideInSearch?: boolean;
  required?: boolean;
  requiredOnCreate?: boolean;
  copyable?: boolean;
  width?: number;
  options?: OptionItem[];
  loadOptions?: () => Promise<OptionItem[]>;
  loadTreeOptions?: () => Promise<TreeOption[]>;
  colSpan?: 12 | 24;
}

export interface ResourceRowAction {
  key: string;
  label: string;
  danger?: boolean;
  visible?: (record: GenericRecord) => boolean;
  successMessage?: string | false;
  reloadAfterAction?: boolean;
  onClick: (record: GenericRecord) => Promise<void>;
}

export interface ResourceToolbarAction {
  key: string;
  label: string;
  type?: 'primary' | 'default' | 'dashed' | 'link' | 'text';
  successMessage?: string | false;
  reloadAfterAction?: boolean;
  onClick: (context: {
    reload: () => void;
    selectedRows: GenericRecord[];
  }) => Promise<void>;
}

export interface ResourceConfig {
  key: string;
  path: string;
  title: string;
  description?: string;
  resourcePath: string;
  idField?: string;
  fields: BackendFieldConfig[];
  allowCreate?: boolean;
  allowView?: boolean;
  allowEdit?: boolean;
  allowDelete?: boolean;
  allowBatchDelete?: boolean;
  allowImport?: boolean;
  allowExport?: boolean;
  detailParams?: Record<string, any>;
  detailLoader?: (id: string | number) => Promise<GenericRecord>;
  transformSubmit?: (
    values: GenericRecord,
    context: { mode: 'create' | 'edit'; currentRecord?: GenericRecord },
  ) => GenericRecord;
  afterSave?: (context: {
    mode: 'create' | 'edit';
    savedId?: string | number;
    values: GenericRecord;
    currentRecord?: GenericRecord;
  }) => Promise<void>;
  rowActions?: ResourceRowAction[];
  toolbarActions?: ResourceToolbarAction[];
}
