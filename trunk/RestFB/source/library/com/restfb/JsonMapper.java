/*
 * Copyright (c) 2010 Mark Allen.
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.restfb;

import java.util.List;

/**
 * Specifies how a Facebook JSON-to-Java (and vice-versa) mapper must operate.
 * <p>
 * Note that implementors must be able to handle illegal JSON in
 * {@link #toJavaObject(String, Class)} and {@link #toJavaList(String, Class)}
 * in order to correctly process Facebook responses. For example, the {@code
 * Users.getLoggedInUser} API call returns a value like {@code 1240077}, which
 * is not valid JSON.
 * 
 * @author <a href="http://restfb.com">Mark Allen</a>
 */
public interface JsonMapper {
  /**
   * Given a JSON string, create and return a new instance of a corresponding
   * Java object of type {@code type}.
   * <p>
   * The Java {@code type} must have a visible no-argument constructor.
   * 
   * @param <T>
   *          Java type to map to.
   * @param json
   *          The JSON to be mapped to a Java type.
   * @param type
   *          Java type token.
   * @return A Java object (of type {@code type}) representation of the JSON
   *         input.
   * @throws FacebookJsonMappingException
   *           If an error occurs while mapping JSON to Java.
   */
  <T> T toJavaObject(String json, Class<T> type)
      throws FacebookJsonMappingException;

  /**
   * Given a JSON string, create and return a new instance of a corresponding
   * Java list which contains elements of type {@code type}.
   * <p>
   * The Java {@code type} must have a visible no-argument constructor.
   * 
   * @param <T>
   *          Java type to map to for each element of the list.
   * @param json
   *          The JSON to be mapped to a Java type.
   * @param type
   *          Java type token.
   * @return A Java object (of type {@code List} which contains elements of type
   *         {@code type}) representation of the JSON input.
   * @throws FacebookJsonMappingException
   *           If an error occurs while mapping JSON to Java.
   */
  <T> List<T> toJavaList(String json, Class<T> type)
      throws FacebookJsonMappingException;

  /**
   * Given a Java {@code object}, create and return a JSON string that
   * represents it.
   * <p>
   * The {@code object}'s properties will be traversed recursively, allowing for
   * arbitrarily complex JSON generation.
   * 
   * @param object
   *          The Java object to map to JSON. Can be a Javabean, {@code List},
   *          or {@code Map}.
   * @return A JSON string.
   * @throws FacebookJsonMappingException
   *           If an error occurs while mapping Java to JSON.
   */
  String toJson(Object object) throws FacebookJsonMappingException;
}