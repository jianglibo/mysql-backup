import * as React from "react";
import { ActionMenuDescription, ActionMenuBarProps } from "./action-menu-desc";
import ActionMenu from "./action-menu";
import * as $ from "jquery";

export default class ActionMenuBar extends React.Component<
  ActionMenuBarProps,
  {  selectedItems: {id: string}[] }
> {
  constructor(props: ActionMenuBarProps) {
    super(props);
    this.state = { selectedItems: [] };
    let tbodyCheckboxes = this.props.tableContainer.find("tbody input[type='checkbox']");
    let theadCheckbox = this.props.tableContainer.find("thead input[type='checkbox']");

    tbodyCheckboxes.change(() => {
        let c = this.props.tableContainer;
        let allNodes = c.find("tbody input[type='checkbox']");
        let ids = this.getSelectedIds();
        if (allNodes.length === ids.length) {
            theadCheckbox.prop('checked', true);
        } else {
            theadCheckbox.prop('checked', false);
        }
        this.setState({selectedItems: ids});
    });
  }

  getSelectedIds(): {id: string}[] {
    let c = this.props.tableContainer;
    let checkedNodes = c.find("tbody input[type='checkbox']:checked");
    let ids: { id: string; }[] = [];
    checkedNodes.each(function(idx, val){
        ids.push({id: $(val).attr("id")});
    });
    return ids;
  }

  render() {
    return (
      <div
        className="pure-button-group button-xsmall action-menu"
        role="group"
        aria-label="..."
      >
        {this.props.menuDescriptions.map(md => (
          <ActionMenu
            baseUrl={this.props.baseUrl}
            menuDescription={md}
            key={md.actionId}
            selectedItems={this.state.selectedItems}
          />
        ))}
      </div>
    );
  }
}
