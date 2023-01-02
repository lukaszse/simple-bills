export function setToZeroIfNull(value: number) {
  return value ? value : 0;
}

export function isPeriodInvalid(dateFrom: Date | string, dateTo: Date | string): boolean {
  if (dateFrom !== null && dateTo !== null) {
    dateFrom = new Date(dateFrom);
    dateTo = new Date(dateTo);
    return dateFrom.getTime() > dateTo.getTime();
  }
}
