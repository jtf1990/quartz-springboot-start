package com.jtf.quartzstarter.support.web;

import org.springframework.web.bind.annotation.RequestMapping;

import static com.jtf.quartzstarter.config.QuartzWebAutoConfigure.DEFAULT_WEB_PREFIX;


@RequestMapping(value = DEFAULT_WEB_PREFIX)
public class DefaultQuartzWebController extends BaseQuartzWebController {

}
