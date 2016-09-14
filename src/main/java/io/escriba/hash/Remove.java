package io.escriba.hash;

import io.escriba.*;
import io.escriba.DataEntry.Status;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

public class Remove {
	private final HashCollection collection;
	private final CompletableFuture<DataEntry> completable;
	private DataEntry entry;
	private final ErrorHandler errorHandler;
	private final Future<DataEntry> future;
	private final String key;
	private final Remover.RemovedHandler removedHandler;

	public Remove(HashCollection collection, String key, Remover.RemovedHandler removedHandler, ErrorHandler errorHandler) {
		this.collection = collection;
		this.key = key;
		this.removedHandler = removedHandler;
		this.errorHandler = errorHandler;

		this.future = new ProxyFuture<>(completable = new CompletableFuture<>());

		if (removedHandler != null)
			collection.executor.submit(this::removeFile);
		else
			throw new EscribaException.IllegalArgument("The removeHandler is null!");
	}

	private void error(Throwable throwable) {
		if (errorHandler != null)
			try {
				errorHandler.apply(throwable);
			} catch (Exception e) {
				// TODO: Log?
			}

		completable.completeExceptionally(throwable);
	}

	public Future<DataEntry> future() {
		return future;
	}


	private void removeFile() {
		entry = collection.getEntry(key);

		if (entry == null) {
			error(new EscribaException.NotFound(key));
			return;
		}

		if (entry.status != Status.Ok) {
			error(new EscribaException.IllegalState(key + " status must be Ok"));
			return;
		}

		Path path = collection.getPath(key);

		if (!Files.exists(path)) {
			error(new EscribaException.IllegalState("File of " + key + " doesn't exist"));
			return;
		}

		if (!Files.isRegularFile(path)) {
			error(new EscribaException.IllegalState("File of " + key + " isn't a regular file"));
			return;
		}

		try {
			collection.updateEntry(key, entry = entry.copy().status(Status.Deleting).end());
		} catch (Exception e) {
			error(new EscribaException.Unexpected(e));
		}

		try {
			Files.delete(path);
		} catch (Exception e) {
			error(e);
			return;
		}

		try {
			collection.removeEntry(key);
		} catch (Exception e) {
			error(new EscribaException.Unexpected(e));
			return;
		}

		try {
			removedHandler.apply(entry);
		} catch (Exception e) {
			error(e);
		}

		completable.complete(entry.copy().status(Status.Deleted).end());
	}
}
