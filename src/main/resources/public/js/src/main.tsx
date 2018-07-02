import { sayHello } from "./greet";

import * as ReactDOM from "react-dom";
import * as React from 'react';
import ActionMenuBar from "./action-men-bar";
import * as $ from "jquery";
import { ActionMenuDescription, ActiveWhen } from "./action-menu-desc";


function showHello(divName: string, name: string) {
    const elt = document.getElementById(divName);
    elt.innerText = sayHello(name);
}

// import { jQuery } from "jquery";
(window as any)['showHello'] = showHello;
// window.alert((window as any)['abc']);
// showHello("greeting", "TypeScript");
let e = document.getElementById("root");

let mds = [new ActionMenuDescription("edit", () => {}, "edit", ActiveWhen.ALWAYS)];

ReactDOM.render(
  <ActionMenuBar menuDescriptions={mds} baseUrl="/app/servers"/>,
  document.getElementById('root')
);