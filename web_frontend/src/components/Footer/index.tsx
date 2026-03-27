import { DefaultFooter } from '@ant-design/pro-components';
import { useModel } from '@umijs/max';
import React from 'react';
import { normalizeBranding } from '@/utils/branding';

const Footer: React.FC = () => {
  const { initialState } = useModel('@@initialState');
  const branding = normalizeBranding(initialState?.branding);

  return (
    <DefaultFooter
      style={{
        background: 'none',
      }}
      copyright={branding.copyrightText || branding.siteName}
      links={[]}
    />
  );
};

export default Footer;
