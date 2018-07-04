import * as React from "react";
import {
  ActionMenuDescription,
  ActionMenuProps,
  ActiveWhen
} from "./action-menu-desc";

export default class ActionMenu extends React.Component<ActionMenuProps, {}> {
  constructor(props: ActionMenuProps) {
    super(props);
  }

  calcClass(): string {
    let cname = "pure-button am-" + this.props.menuDescription.actionId;
    let activeOn = this.props.menuDescription.activeOn;
    let selectedItems = this.props.selectedItems;
    let disabledClass = " pure-button-disabled";
    switch (activeOn) {
      case ActiveWhen.SINGLE:
        if (selectedItems.length !== 1) {
          cname += disabledClass;
        }
        break;
      case ActiveWhen.NOT_EMPTY:
        if (selectedItems.length < 1) {
          cname += disabledClass;
        }
        break;
      default:
        break;
    }
    return cname;
  }

  calcHref(): string {
    let href = "#";
    let md = this.props.menuDescription;
    let items = this.props.selectedItems;
    switch (md.actionId) {
      case "create":
        href = this.props.baseUrl + "/create";
        break;
      case "edit":
        if (items.length === 1) {
            href =
            this.props.baseUrl + "/" + items[0].id + "/edit/";
        }
      default:
        break;
    }
    return href;
  }

  render() {
    return (
      <button className={this.calcClass()}>
        {this.props.menuDescription.icon && (
          <i className={this.props.menuDescription.icon} />
        )}
        <a href={this.calcHref()}>{this.props.menuDescription.name}</a>
      </button>
    );
  }
}

/**
 * lifting the states to parent. So I need only props.
 */
