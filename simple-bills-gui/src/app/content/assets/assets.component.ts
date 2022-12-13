import {Component, OnInit} from '@angular/core';
import {Deposit, DepositType} from "../../../dto/deposit";
import {DepositService} from "../../../service/deposit.service";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";

@Component({
  selector: 'app-assets',
  templateUrl: './assets.component.html',
  styleUrls: ['./assets.component.scss']
})
export class AssetsComponent implements OnInit {

  deposit: Deposit = {
    name: null,
    depositType: null,
    value: null,
    bankName: null,
    durationInMonths: null,
    annualInterestRate: null
  }

  selectedDeposit: string = null;

  constructor(public depositService: DepositService,
              private modalService: NgbModal) {
  }

  ngOnInit(): void {
    this.depositService.refresh();
  }

  openDepositCreationWindow(content) {
    this.resetFormFields()
    this.modalService.open(content, {ariaLabelledBy: 'modal-deposit-creation'}).result
      .then(
        () => this.depositService.createDeposit(this.deposit)
          .subscribe(() => this.ngOnInit()),
        () => console.log(`Deposit creation canceled`));
  }

  openDepositEditWindow(deposit: Deposit, content) {
    this.selectedDeposit = deposit.name;
    this.setFormFields(deposit)
    console.log(this.deposit)
    this.modalService.open(content, {ariaLabelledBy: 'modal-deposit-update'}).result
      .then(
        () => this.depositService.updateDeposit(this.deposit)
          .subscribe(() => this.ngOnInit()),
        () => console.log("Deposit update canceled"));
  }

  openDepositDeletionConfirmationWindow(depositName: string, content) {
    this.selectedDeposit = depositName;
    this.modalService.open(content, {ariaLabelledBy: "modal-deposit-deletion"}).result
      .then(
        () => this.depositService.deleteDeposit(depositName)
          .subscribe(() => this.ngOnInit()),
        () => console.log("Deposit deletion canceled"));
  }

  resetFormFields() {
    this.deposit.name = null;
    this.deposit.depositType = DepositType.TERM;
    this.deposit.value = null;
    this.deposit.durationInMonths = null;
    this.deposit.annualInterestRate = null;
    this.deposit.bankName = null;
  }

  setFormFields(deposit: Deposit) {
    this.deposit.name = deposit.name;
    this.deposit.bankName = deposit.bankName;
    this.deposit.depositType = deposit.depositType;
    this.deposit.value = deposit.value;
    this.deposit.durationInMonths = deposit.durationInMonths;
    this.deposit.annualInterestRate = deposit.annualInterestRate;
  }
}
