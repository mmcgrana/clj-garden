(ns something.base64
  (:import org.apache.commons.codec.binary.Base64))

(defn base64-encode
  [unencoded]
  "Returns the byte array of base64 encoded data for the given byte array"
  (Base64/encodeBase64Chunked unencoded))

(defn base64-decode
  [encoded]
  (Base64/decodeBase64 encoded))