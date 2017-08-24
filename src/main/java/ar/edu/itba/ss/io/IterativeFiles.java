package ar.edu.itba.ss.io;

import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

public class IterativeFiles {

  public static Path firstNotExists(final String name) {
    int i = 1;
    Path path = FileSystems.getDefault().getPath(name + "_" + i++);

    while (Files.exists(path)) {
      path = FileSystems.getDefault().getPath(name + "_" + i++);
    }

    return path;
  }
}
