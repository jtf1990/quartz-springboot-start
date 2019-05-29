package com.jtf.quartzstarter.support.web;

import com.jtf.quartzstarter.factory.SchedulerFactoryBean;
import org.quartz.Scheduler;
import org.springframework.web.bind.annotation.RequestMapping;


@RequestMapping(value = "${quartz.config.web-prefix}")
public class CustomerQuartzWebController extends BaseQuartzWebController {


}
