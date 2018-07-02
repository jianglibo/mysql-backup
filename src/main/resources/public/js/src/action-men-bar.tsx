import * as React from "react";
import { ActionMenuDescription, ActionMenuBarProps } from "./action-menu-desc"
import ActionMenu from "./action-menu";

export default class ActionMenuBar extends React.Component<ActionMenuBarProps, {}> {

    render() {
        return <div className="pure-button-group button-xsmall action-menu" role="group" aria-label="...">
            {
                this.props.menuDescriptions.map((md) =>
                    <ActionMenu baseUrl={this.props.baseUrl} menuDescription={md} key={md.actionId} />
                )
            }
        </div>
    }
}

