package io.escriba;

public interface Collection {

	Get get(String key, Get.ReadyHandler readyHandler, Get.ReadHandler readHandler) throws Exception;

	Get get(String key, Get.ReadyHandler readyHandler, Get.ReadHandler readHandler, ErrorHandler errorHandler) throws Exception;

	Put put(String key, Put.ReadyHandler readyHandler, Put.WrittenHandler writtenHandler) throws Exception;

	Put put(String key, Put.ReadyHandler readyHandler, Put.WrittenHandler writtenHandler, ErrorHandler errorHandler) throws Exception;

}
