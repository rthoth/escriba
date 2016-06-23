package io.escriba.store;

public abstract class StoreCollection {

	public abstract Get get(String key);

	public abstract Put put(String key);
}
