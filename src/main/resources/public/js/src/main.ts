import { sayHello } from "./greet";

function showHello(divName: string, name: string) {
    const elt = document.getElementById(divName);
    elt.innerText = sayHello(name);
}

import * as $ from "jquery";
// import { jQuery } from "jquery";
(<any>window)['showHello'] = showHello;
// showHello("greeting", "TypeScript");