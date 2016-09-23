package io.escriba.node;

public interface PostcardWriter<T, P> {
	WriteAction<P> apply(T value, P previous) throws Exception;
}
