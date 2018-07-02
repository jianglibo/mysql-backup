enum ActiveWhen {
    ALWAYS,
    SINGLE,
    NOT_EMPTY
}
class ActionMenuDescription {
    constructor(public actionId: string, public onClick: () => void, public icon: string, public activeOn: ActiveWhen) {
    }
}

class ActionMenuProps {
    constructor(public baseUrl: string, public menuDescription: ActionMenuDescription){}
}

class ActionMenuBarProps {
    constructor(public baseUrl: string, public menuDescriptions: ActionMenuDescription[]){}
}

export { ActionMenuDescription, ActiveWhen, ActionMenuProps, ActionMenuBarProps }