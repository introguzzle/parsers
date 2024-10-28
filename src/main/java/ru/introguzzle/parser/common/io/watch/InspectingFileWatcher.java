package ru.introguzzle.parser.common.io.watch;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.introguzzle.parser.common.io.IO;

import java.nio.file.*;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import ru.introguzzle.parser.common.io.watch.Report.Type;

/**
 * The {@code InspectingFileWatcher} class extends the {@link FileWatcher} to monitor a specific file for changes,
 * including creation, modification, and deletion events. It generates reports that contain detailed information
 * about the file events, such as timestamps, types, and differences in content.
 *
 * <p><strong>Example Usage:</strong></p>
 *
 * <pre>{@code
 * import ru.introguzzle.parser.common.io.IO;
 * import ru.introguzzle.parser.common.io.watch.InspectingFileWatcher;
 * import ru.introguzzle.parser.common.io.watch.Report;
 * import ru.introguzzle.parser.common.io.watch.Report.Type;
 *
 * import java.nio.file.Path;
 *
 * public class FileWatcherExample {
 *     public static void main(String[] args) {
 *         // Define the path to the file to watch
 *         Path path = Path.of(IO.getRoot().orElseThrow()).resolve("testfile.txt");
 *
 *         // Create an instance of InspectingFileWatcher with custom handlers
 *         InspectingFileWatcher watcher = new InspectingFileWatcher(path, "FileWatcherExample",
 *                 () -> System.out.println("Watcher started")
 *         ) {
 *             @Override
 *             public @NotNull Runnable onCloseHandler() {
 *                 return () -> System.out.println("Watcher closed");
 *             }
 *
 *             @Override
 *             public @NotNull Consumer<? super IOException> onError() {
 *                 return e -> System.err.println("Error occurred: " + e.getMessage());
 *             }
 *         };
 *
 *         // Add a handler to process file events
 *         watcher.addOnChangeHandler(report -> {
 *             System.out.println("Event detected: " + report);
 *             if (report.getType() == Type.MODIFY) {
 *                 InspectingFileWatcher.ModifyReport modifyReport = (InspectingFileWatcher.ModifyReport) report;
 *                 String content = new String(modifyReport.data);
 *                 System.out.println("File content after modification:\n" + content);
 *             }
 *         });
 *
 *         // Start the watcher
 *         watcher.start();
 *
 *         // Let the watcher run for a specified duration (e.g., 60 seconds)
 *         try {
 *             Thread.sleep(60 * 1000);
 *         } catch (InterruptedException e) {
 *             Thread.currentThread().interrupt();
 *         }
 *
 *         // Stop the watcher
 *         watcher.stopThread();
 *     }
 * }
 * }</pre>
 */
public abstract class InspectingFileWatcher extends FileWatcher<Report> {

    /**
     * An abstract implementation of the {@link Report} interface that provides common fields and methods
     * for different types of file event reports.
     */
    public static abstract class AbstractReport implements Report {
        /** The timestamp of the event. */
        public final FileTime timestamp;
        /** The type of the event (CREATE, MODIFY, DELETE). */
        public final Type type;
        /** The path to the file that the event occurred on. */
        public final Path path;
        /** The previous report in the sequence of events, if any. */
        public final Report previous;

        /**
         * Constructs an {@code AbstractReport} with the specified parameters.
         *
         * @param timestamp the timestamp of the event
         * @param type      the type of the event
         * @param path      the path to the file
         * @param previous  the previous report, or {@code null} if none
         */
        public AbstractReport(FileTime timestamp,
                              Type type,
                              Path path,
                              Report previous) {
            this.timestamp = timestamp;
            this.type = type;
            this.path = path;
            this.previous = previous;
        }

        @Override
        public Type getType() {
            return type;
        }

        @Override
        public Report getPrevious() {
            return previous;
        }

        @Override
        public Date getTimestamp() {
            return Date.from(timestamp.toInstant());
        }

        @Override
        public Path getPath() {
            return path;
        }

        @NotNull
        @Override
        public Iterator<Report> iterator() {
            List<Report> list = toList();
            return list.iterator();
        }

        @NotNull
        public Iterator<Report> iteratorFromLatest() {
            List<Report> list = toList().reversed();
            return list.iterator();
        }

        /**
         * Returns the details specific to the report type.
         *
         * @return a string containing the details of the report
         */
        protected abstract String getDetails();

        @Override
        public @NotNull String toString() {
            return String.format("[%s] [%s] [%s]",
                    timestamp,
                    type.name(),
                    getDetails()
            );
        }
    }

    /**
     * A report representing a file modification event. It contains the new data of the file,
     * a hash of the data, and can compute the difference in size compared to the previous report.
     */
    public static class ModifyReport extends AbstractReport {
        /** The data of the file after modification. */
        public final byte[] data;
        /** The hash code of the data. */
        public final int hash;

        /**
         * Constructs a {@code ModifyReport} with the specified parameters.
         *
         * @param timestamp the timestamp of the event
         * @param path      the path to the file
         * @param data      the data of the file
         * @param previous  the previous report, or {@code null} if none
         */
        public ModifyReport(FileTime timestamp,
                            Path path,
                            byte[] data,
                            Report previous) {
            super(timestamp, Type.MODIFY, path, previous);
            this.data = data;
            this.hash = Arrays.hashCode(data);
        }

        /**
         * Calculates the difference in data size compared to the previous {@code ModifyReport}.
         *
         * @return an {@code Optional} containing the size difference, or empty if not applicable
         */
        public Optional<Integer> difference() {
            return previous != null && previous instanceof ModifyReport modifyReport
                    ? Optional.of(data.length - modifyReport.data.length)
                    : Optional.empty();
        }

        @Override
        protected String getDetails() {
            Optional<Integer> value = difference();
            if (value.isPresent()) {
                String prefix = value.get() > 0 ? "+" : "";
                return prefix + value.get();
            }

            return previous == null ? "" : previous.getType().name();
        }

        @Override
        public int hashCode() {
            return hash;
        }
    }

    /**
     * A report representing a file creation event.
     */
    public static class CreateReport extends AbstractReport {
        /**
         * Constructs a {@code CreateReport} with the specified parameters.
         *
         * @param timestamp the timestamp of the event
         * @param path      the path to the file
         * @param previous  the previous report, or {@code null} if none
         */
        public CreateReport(FileTime timestamp, Path path, Report previous) {
            super(timestamp, Type.CREATE, path, previous);
        }

        @Override
        protected String getDetails() {
            return "CREATED";
        }
    }

    /**
     * A report representing a file deletion event.
     */
    public static class DeleteReport extends AbstractReport {
        /**
         * Constructs a {@code DeleteReport} with the specified parameters.
         *
         * @param timestamp the timestamp of the event
         * @param path      the path to the file
         * @param previous  the previous report, or {@code null} if none
         */
        public DeleteReport(FileTime timestamp, Path path, Report previous) {
            super(timestamp, Type.DELETE, path, previous);
        }

        @Override
        protected String getDetails() {
            return "DELETED";
        }
    }

    /** The current report, representing the most recent event. */
    private Report report;
    /** A list of handlers to be called when an event occurs. */
    private final List<Consumer<? super Report>> onChangeHandlers = new ArrayList<>();

    /**
     * Constructs an {@code InspectingFileWatcher} for the specified file path.
     *
     * @param path the path to the file to watch
     */
    public InspectingFileWatcher(@NotNull Path path) {
        super(path);
    }

    /**
     * Constructs an {@code InspectingFileWatcher} with a custom name.
     *
     * @param path the path to the file to watch
     * @param name the name of the watcher thread
     */
    public InspectingFileWatcher(@NotNull Path path, @NotNull String name) {
        super(path, name);
    }

    /**
     * Constructs an {@code InspectingFileWatcher} with a custom name and a startup action.
     *
     * @param path    the path to the file to watch
     * @param name    the name of the watcher thread
     * @param onStart an action to run when the watcher starts
     */
    public InspectingFileWatcher(@NotNull Path path, @NotNull String name, @Nullable Runnable onStart) {
        super(path, name, onStart);
    }

    @Override
    public @NotNull BiFunction<Path, Type, ? extends Report> mapper() {
        return (path, type) -> {
            FileTime timestamp = type == Type.DELETE
                    ? FileTime.from(Instant.now())
                    : IO.getLastModifiedTime(path);

            return switch (type) {
                case MODIFY -> {
                    byte[] bytes = IO.readAllBytes(path);
                    report = new ModifyReport(timestamp, path, bytes, report);

                    yield report;
                }

                case CREATE -> {
                    report = new CreateReport(timestamp, path, report);
                    yield report;
                }
                case DELETE -> {
                    report = new DeleteReport(timestamp, path, report);
                    yield report;
                }
            };
        };
    }

    @Override
    public final @NotNull Consumer<? super Report> eventHandler() {
        return report -> {
            for (Consumer<? super Report> handler : onChangeHandlers) {
                handler.accept(report);
            }
        };
    }

    /**
     * Adds a handler to be invoked when a file event occurs.
     *
     * @param handler the handler to add
     */
    public final void addOnChangeHandler(Consumer<? super Report> handler) {
        onChangeHandlers.add(handler);
    }
}
