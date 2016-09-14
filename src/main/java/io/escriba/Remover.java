package io.escriba;

import java.util.concurrent.Future;

public interface Remover {
	Remover complete(RemovedHandler removedHandler);

	Remover error(ErrorHandler errorHandler);

	Future<DataEntry> start();

	interface RemovedHandler {
		void apply(DataEntry dataEntry) throws Exception;
	}
}
