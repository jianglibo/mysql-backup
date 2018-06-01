package com.go2wheel.mysqlbackup.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class SampleController {
    @RequestMapping("/")
    @ResponseBody
    String home() {
        return "Hello World!";
    }
    
    
    @GetMapping("/content")
    public ModelAndView getContent(@RequestParam(required=false) String tpl) {
        ModelAndView mav = new ModelAndView();
        if (tpl == null) {
        	mav.setViewName("content");
        } else {
        	mav.setViewName(tpl);
        }
        return mav;
    }
}
