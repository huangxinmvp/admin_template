export const getRequestErrorMessage = (error: any, fallback: string) => {
  const infoMessage = error?.info?.errorMessage;
  const responseMessage = error?.response?.data?.message || error?.response?.data?.errorMessage;
  const directMessage = typeof error?.message === 'string' ? error.message : undefined;
  return infoMessage || responseMessage || directMessage || fallback;
};
