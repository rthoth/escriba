package io.escriba.functional;

public interface Callback3<A, B, C> {
	void apply(A a, B b, C c) throws Exception;
}
