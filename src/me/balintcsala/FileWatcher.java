package me.balintcsala;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;

import static java.nio.file.StandardWatchEventKinds.*;

public class FileWatcher {

    public enum ChangeType {
        CREATE,
        DELETE,
        MODIFY,
    }

    public interface ChangeEventListener {

        void onChange(Path path, ChangeType changeType);

    }

    public FileWatcher(Path directory, ArrayList<String> excludes, ChangeEventListener listener) {
        Thread thread = new Thread(() -> {
            try (final WatchService watchService = FileSystems.getDefault().newWatchService()) {
                Files.walkFileTree(directory, new FileVisitor<Path>() {
                    @Override
                    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                        dir.register(watchService, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                        return FileVisitResult.CONTINUE;
                    }
                });
                while (true) {
                    WatchKey key;
                    try {
                        key = watchService.take();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        return;
                    }
                    for (WatchEvent<?> event : key.pollEvents()) {
                        WatchEvent.Kind<?> kind = event.kind();
                        if (kind == OVERFLOW)
                            continue;

                        ChangeType changeType;
                        if (kind == ENTRY_CREATE) {
                            changeType = ChangeType.CREATE;
                        } else if (kind == ENTRY_MODIFY) {
                            changeType = ChangeType.MODIFY;
                        } else if (kind == ENTRY_DELETE) {
                            changeType = ChangeType.DELETE;
                        } else {
                            // Unknown modification
                            continue;
                        }

                        WatchEvent<Path> pathEvent = (WatchEvent<Path>) event;
                        Path relativePath = pathEvent.context();
                        boolean exclude = false;
                        for (String regex : excludes) {
                            if (relativePath.toString().matches(regex)) {
                                exclude = true;
                                break;
                            }
                        }
                        if (exclude)
                            continue;

                        if (relativePath.toString().endsWith("~")) {
                            // This is a backup file, skip it
                            continue;
                        }
                        Path absolutePath = ((Path) key.watchable()).resolve(relativePath);
                        listener.onChange(absolutePath, changeType);
                    }
                    key.reset();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        thread.start();

    }

}
