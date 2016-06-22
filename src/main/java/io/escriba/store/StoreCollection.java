package io.escriba.store;

public abstract class StoreCollection {

	public abstract void put(String key, Store.Putter putter, Store.Fail fail);

	public void put(String key, Store.Putter putter) {
		put(key, putter, null);
	}
}
