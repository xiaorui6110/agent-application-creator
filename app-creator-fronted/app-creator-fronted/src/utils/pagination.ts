export type TotalCountResolveParams = {
  current: number
  pageSize: number
  totalRow?: number
  recordsLength: number
}

export const resolveTotalCount = (params: TotalCountResolveParams) => {
  const { current, pageSize, totalRow, recordsLength } = params
  if (recordsLength <= 0) return 0

  if (recordsLength < pageSize) {
    return (Math.max(current, 1) - 1) * pageSize + recordsLength
  }

  return totalRow ?? recordsLength
}

