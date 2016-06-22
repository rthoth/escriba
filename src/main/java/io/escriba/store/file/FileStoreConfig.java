package io.escriba.store.file;

public final class FileStoreConfig {
	public static final FileStoreConfig DEFAULT = new FileStoreConfig(2, 3);

	public int colIdGenParts;
	public int valIdGenParts;

	public FileStoreConfig(int colIdGenParts, int valIdGenParts) {
		this.colIdGenParts = colIdGenParts;
		this.valIdGenParts = valIdGenParts;
	}
}
