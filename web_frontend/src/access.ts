/**
 * @see https://umijs.org/docs/max/access#access
 * */
export default function access(
  initialState: { currentUser?: API.CurrentUser } | undefined,
) {
  const { currentUser } = initialState ?? {};
  const roles = currentUser?.roles || [];
  const permissions = currentUser?.permissions || [];
  const canAdmin = currentUser?.access === 'admin' || roles.includes('ADMIN');

  return {
    canAdmin,
    hasAuthority: (authority: string) =>
      canAdmin || permissions.includes(authority),
  };
}
