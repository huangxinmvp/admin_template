import type { BrandingConfig, SystemConfigGroup } from '@/services/backend/types';

export const DEFAULT_BRANDING: BrandingConfig = {
  siteName: 'SaaS Admin Template',
  siteSubtitle: '开箱即用的 SaaS 后台管理模板',
  logoUrl: '/logo.svg',
  primaryColor: '#1677ff',
  copyrightText: 'SaaS Admin Template 版权所有',
};

export const normalizeBranding = (
  branding?: Partial<BrandingConfig> | null,
): BrandingConfig => ({
  ...DEFAULT_BRANDING,
  ...(branding || {}),
});

export const extractBrandingFromGroups = (
  groups: SystemConfigGroup[],
): BrandingConfig => {
  const allItems = groups.flatMap((group) => group.items || []);
  const getValue = (configKey: string, fallback = '') =>
    allItems.find((item) => item.configKey === configKey)?.configValue ||
    allItems.find((item) => item.configKey === configKey)?.defaultValue ||
    fallback;

  return normalizeBranding({
    siteName: getValue('branding.siteName', DEFAULT_BRANDING.siteName),
    siteSubtitle: getValue(
      'branding.siteSubtitle',
      DEFAULT_BRANDING.siteSubtitle || '',
    ),
    logoUrl: getValue('branding.logoUrl', DEFAULT_BRANDING.logoUrl || ''),
    primaryColor: getValue(
      'branding.primaryColor',
      DEFAULT_BRANDING.primaryColor || '',
    ),
    copyrightText: getValue(
      'branding.copyrightText',
      DEFAULT_BRANDING.copyrightText || '',
    ),
  });
};
