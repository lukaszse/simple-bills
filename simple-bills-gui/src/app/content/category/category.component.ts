import {Component, OnInit} from '@angular/core';
import {Category} from "../../../dto/category";
import {CategoryService} from "../../../service/category.service";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {TransactionType} from "../../../dto/transaction";

@Component({
  selector: 'app-category',
  templateUrl: './category.component.html',
  styleUrls: ['./category.component.css']
})
export class CategoryComponent implements OnInit {

  category: Category = {
    name: null,
    transactionType: null,
    limit: null
  };

  categories: Category[];
  allowableReplacementCategories: Category[];
  totalLimit: number;
  categoryToDelete: string = null;
  replacementCategory: string = null;
  expenseTransactionType: boolean;


  constructor(private categoryService: CategoryService,
              private modalService: NgbModal) {
  }

  ngOnInit(): void {
    this.categoryService.findCategories().subscribe(
      (categories) => {
        this.categories = categories;
        this.totalLimit = CategoryComponent.countTotalLimit(categories);
      }
    );
  }

  openCreationWindow(transactionType: "INCOME" | "EXPENSE", content) {
    const transactionTypeEnum = TransactionType[transactionType];
    this.expenseTransactionType = transactionTypeEnum === TransactionType.EXPENSE;
    this.resetFormFields(transactionTypeEnum)
    this.modalService.open(content, {ariaLabelledBy: 'modal-category-creation'}).result
      .then(
        () => this.categoryService.createCategory(this.category)
          .subscribe((deletionResponse) => this.ngOnInit()),
        () => console.log("Exit without any action.")
      );
  }

  openEditWindowForSelectedCategory(category: Category, content) {
    this.setFormCategoryFields(category);
    this.expenseTransactionType = category.transactionType === TransactionType.EXPENSE;
    this.modalService.open(content, {ariaLabelledBy: 'modal-category-update'}).result
      .then(
        () => this.categoryService.updateCategory(this.category)
          .subscribe((updateResponse) => this.ngOnInit()),
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
          .subscribe((deletionResponse) => this.ngOnInit()),
        () => console.log("Exit without any action.")
      );
  }

  private setFormCategoryFields(category: Category) {
    this.category.name = category.name;
    this.category.transactionType = category.transactionType;
    this.category.limit = category.limit;
  }

  private static countTotalLimit(categories: Category[]): number {
    return categories
      .map((category) => category.limit)
      .map(limit => limit == null ? 0 : limit)
      .reduce((accumulator, currentValue) => accumulator + currentValue, 0);
  }

  resetFormFields(transactionType: TransactionType) {
    this.category.name = null;
    this.category.transactionType = transactionType;
    this.category.limit = null;
  }
}
