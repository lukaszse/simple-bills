export interface DepositModel {
  name?: string;
  depositType?: DepositType;
  value?: number,
  bankName?: string;
  durationInMonths?: number;
  annualInterestRate?: number
}

export enum DepositType {
  PERPETUAL = "PERPETUAL",
  TERM = "TERM"
}
