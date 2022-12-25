import { Component, OnDestroy, OnInit } from '@angular/core';
import { CategoryModel } from "../../../dto/category.model";
import { CategoryService } from "../../../service/category.service";
import { NgbModal } from "@ng-bootstrap/ng-bootstrap";
import { TransactionType } from "../../../dto/transaction.model";
import { first, Subscription } from "rxjs";

@Component({
  selector: 'app-category',
  templateUrl: './category.component.html',
  styleUrls: ['./category.component.scss']
})
export class CategoryComponent implements OnInit, OnDestroy {

  category: CategoryModel = {
    name: null,
    transactionType: null,
    limit: null
  };

  categories: CategoryModel[];
  allowableReplacementCategories: CategoryModel[];
  totalLimit: number;
  categoryToDelete: string = null;
  replacementCategory: string = null;
  expenseTransactionType: boolean;

  findCategorySubscription: Subscription;


  constructor(private categoryService: CategoryService,
              private modalService: NgbModal) {
  }

  ngOnInit(): void {
    this.findCategorySubscription = this.categoryService.findCategories()
      .subscribe(
        (categories) => {
          this.categories = categories;
          this.totalLimit = CategoryComponent.countTotalLimit(categories);
        }
      );
  }

  ngOnDestroy(): void {
    this.findCategorySubscription.unsubscribe();
  }

  openCreationWindow(transactionType: "INCOME" | "EXPENSE", content) {
    const transactionTypeEnum = TransactionType[transactionType];
    this.expenseTransactionType = transactionTypeEnum === TransactionType.EXPENSE;
    this.resetFormFields(transactionTypeEnum)
    this.modalService.open(content, {ariaLabelledBy: 'modal-category-creation'}).result
      .then(
        () => this.categoryService.createCategory(this.category)
          .pipe(first())
          .subscribe(() => this.ngOnInit()),
        () => console.log("Exit without any action.")
      );
  }

  openEditWindowForSelectedCategory(category: CategoryModel, content) {
    this.setFormCategoryFields(category);
    this.expenseTransactionType = category.transactionType === TransactionType.EXPENSE;
    this.modalService.open(content, {ariaLabelledBy: 'modal-category-update'}).result
      .then(
        () => this.categoryService.updateCategory(this.category)
          .pipe(first())
          .subscribe(() => this.ngOnInit()),
        () => console.log("Exit without any action.")
      );
  }

  openDeletionConfirmationWindow(categoryName: string, content) {
    this.categoryToDelete = categoryName;
    this.replacementCategory = null;
    this.allowableReplacementCategories = this.categories.filter(category => category.name !== categoryName);
    this.modalService.open(content, {ariaLabelledBy: 'modal-category-deletion'}).result
      .then(
        () => this.categoryService.deleteCategory(categoryName, this.replacementCategory)
          .pipe(first())
          .subscribe(() => this.ngOnInit()),
        () => console.log("Exit without any action.")
      );
  }

  private setFormCategoryFields(category: CategoryModel) {
    this.category.name = category.name;
    this.category.transactionType = category.transactionType;
    this.category.limit = category.limit;
  }

  private resetFormFields(transactionType: TransactionType) {
    this.category.name = null;
    this.category.transactionType = transactionType;
    this.category.limit = null;
  }

  private static countTotalLimit(categories: CategoryModel[]): number {
    return categories
      .map((category) => category.limit)
      .map(limit => limit == null ? 0 : limit)
      .reduce((accumulator, currentValue) => accumulator + currentValue, 0);
  }
}
