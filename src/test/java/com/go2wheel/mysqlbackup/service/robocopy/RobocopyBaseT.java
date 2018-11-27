package com.go2wheel.mysqlbackup.service.robocopy;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.rules.TemporaryFolder;
import org.springframework.beans.factory.annotation.Value;

import com.go2wheel.mysqlbackup.SpringBaseFort;
import com.go2wheel.mysqlbackup.model.RobocopyDescription;
import com.go2wheel.mysqlbackup.model.RobocopyItem;
import com.google.common.collect.Lists;

public class RobocopyBaseT extends SpringBaseFort {
	
    @Value("${myapp.app.archive}")
    protected String zipApp;

	protected RobocopyDescription grpd(TemporaryFolder repofolder, TemporaryFolder srcfolder) throws IOException {
		RobocopyDescription rd = new RobocopyDescription.RobocopyDescriptionBuilder(0, repofolder.getRoot().toPath().toAbsolutePath().toString()).build();
		String compress = String.format("& '%s' a -ms %%s %%s", zipApp);
		rd.setCompressCommand(compress);
		String expand = String.format("& '%s' x -o+ %%s %%s", zipApp);
		rd.setExpandCommand(expand);
		rd.setArchiveName("hello.rar");
		//	& 'C:\Program Files\WinRAR\Rar.exe' x -o+ upload  .\\upload ALWAYS TREAT extract destination as a folder.
		// -ms
//        If <list> is not specified, -ms switch will use the default
//        set of extensions, which includes the following file types:
//
//        7z, ace, arj, bz2, cab, gz, jpeg, jpg, lha, lz, lzh, mp3,
//        rar, taz, tgz, xz, z, zip, zipx
		RobocopyItem ri = new RobocopyItem(0, srcfolder.getRoot().toPath().toAbsolutePath().toString(), "abc");
		RobocopyItem ri1 = new RobocopyItem(0, srcfolder.getRoot().toPath().toAbsolutePath().toString(), "abc1");
		rd.setRobocopyItems(Lists.newArrayList(ri, ri1));
		Files.createDirectories(Paths.get(rd.getWorkingSpaceAbsolute()));
		return rd;
	}
	
	protected Path createDemoSrc(TemporaryFolder srcfolder) throws IOException {
		Path rt = srcfolder.getRoot().toPath();
		createALocalFile(rt.resolve("a/afile.txt"), "abc");
		return rt;
	}


}
