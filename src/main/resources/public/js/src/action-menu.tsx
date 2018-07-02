import * as React from "react";
import { ActionMenuDescription, ActionMenuProps } from "./action-menu-desc";

export default class ActionMenu extends React.Component<ActionMenuProps, {}> {
    constructor(props: ActionMenuProps) {
        super(props);
        this.state = {date: new Date()};
    }
    render() {
        let cname = "pure-button am-" + this.props.menuDescription.actionId;
        return <button className={cname}>
            {this.props.menuDescription.icon &&
                <i className="far fa-plus-square"></i> }
            <a href="/app/servers/create">新建</a>
        </button>
    }
}

