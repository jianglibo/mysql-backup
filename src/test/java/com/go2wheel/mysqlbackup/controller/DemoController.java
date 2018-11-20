package com.go2wheel.mysqlbackup.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.go2wheel.mysqlbackup.dbservice.RobocopyDescriptionDbService;
import com.go2wheel.mysqlbackup.dbservice.RobocopyItemDbService;
import com.go2wheel.mysqlbackup.dbservice.ServerDbService;
import com.go2wheel.mysqlbackup.dbservice.SoftwareDbService;
import com.go2wheel.mysqlbackup.model.RobocopyDescription;
import com.go2wheel.mysqlbackup.model.RobocopyItem;
import com.go2wheel.mysqlbackup.model.Server;

@Controller
@RequestMapping("/app/demo")
public class DemoController {

	@Autowired
	private ServerDbService serverDbService;

	@Autowired
	protected SoftwareDbService softwareDbService;
	
	@Autowired
	private RobocopyItemDbService robocopyItemDbService;
	
	@Autowired
	private RobocopyDescriptionDbService robocopyDescriptionDbService;

	@GetMapping("/robocopy")
	public String createLocalRobocopy() throws IOException  {
		Path src = Paths.get("c:/robosrc/src/a");
		Path src1 = Paths.get("c:/robosrc/src1/a");
		if (!Files.exists(src)) {
			Files.createDirectories(src);
		}
		if (!Files.exists(src1)) {
			Files.createDirectories(src1);
		}
		Files.write(src.resolve("a.txt"), "abc".getBytes());
		Files.write(src1.resolve("a.txt"), "abc".getBytes());
		
		Server ls = serverDbService.findByHost("localhost");
		if (ls == null) {
			ls = new Server("localhost", "benji");
			ls.setOs("win");
			ls.setServerRole(Server.ROLE_GET);
			ls = serverDbService.save(ls);
		}
		
		RobocopyDescription rd = robocopyDescriptionDbService.findByServerId(ls.getId());
		
		if (rd == null) {
			rd = new RobocopyDescription.RobocopyDescriptionBuilder(ls.getId(), "c:/roborepo").build();
			rd = robocopyDescriptionDbService.save(rd);
		}
		
		List<RobocopyItem> items = robocopyItemDbService.findByDescriptionId(rd.getId());
		if (items.size() == 0) {
			RobocopyItem ri = new RobocopyItem(rd.getId(), "c:/robosrc/src" , "src");
			RobocopyItem ri1 = new RobocopyItem(rd.getId(), "c:/robosrc/src1" , "src1");
			robocopyItemDbService.save(ri);
			robocopyItemDbService.save(ri1);
		}
		
		
		return "redirect:/";
	}
}
