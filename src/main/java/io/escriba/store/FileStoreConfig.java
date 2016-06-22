package io.escriba.store;

public final class FileStoreConfig {
	public static final FileStoreConfig DEFAULT = new FileStoreConfig((short) 3, (short) 5);

	public short collectionIdGenParts;
	public short valueIdGenParts;

	public FileStoreConfig(short cIgGenParts, short vIdGenParts) {
		this.collectionIdGenParts = cIgGenParts;
		this.valueIdGenParts = vIdGenParts;
	}
}
