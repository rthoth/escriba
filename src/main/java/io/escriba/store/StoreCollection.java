package io.escriba.store;

public abstract class StoreCollection {

	public abstract void get(String key, Store.Getter getter, Store.Fail fail);

	public void get(String key, Store.Getter getter) {
		get(key, getter, null);
	}

	public void put(String key, Store.Putter putter) {
		put(key, putter, null);
	}

	public abstract void put(String key, Store.Putter putter, Store.Fail fail);
}
