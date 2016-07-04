package io.escriba;

public interface Remover {
	Remover complete(RemovedHandler removedHandler);

	Remover error(ErrorHandler errorHandler);

	Remover start();

	interface RemovedHandler {
		void apply(DataEntry dataEntry) throws Exception;
	}
}
