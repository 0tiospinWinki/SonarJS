/*
 * SonarQube JavaScript Plugin
 * Copyright (C) 2011-2022 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.plugins.javascript.eslint.cache;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import javax.annotation.Nullable;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

class JsonSerialization<P> extends AbstractSerialization implements CacheWriter<P, Void>, CacheReader<Void, P> {

  private static final Logger LOG = Loggers.get(JsonSerialization.class);

  private final Class<P> jsonClass;
  private final Gson gson = new Gson();

  JsonSerialization(Class<P> jsonClass, SensorContext context, CacheKey cacheKey) {
    super(context, cacheKey);
    this.jsonClass = jsonClass;
  }

  @Override
  public P readFromCache(@Nullable Void config) throws IOException {
    try (var input = getContext().previousCache().read(getCacheKey().toString())) {
      var value = gson.fromJson(new InputStreamReader(input, StandardCharsets.UTF_8), jsonClass);
      LOG.debug("Cache entry extracted for key '{}'", getCacheKey());
      return value;
    } catch (JsonParseException e) {
      throw new IOException("Failure when parsing cache entry JSON", e);
    }
  }

  @Override
  public Void writeToCache(@Nullable P payload) {
    getContext().nextCache().write(getCacheKey().toString(), gson.toJson(payload).getBytes(StandardCharsets.UTF_8));
    LOG.debug("Cache entry created for key '{}'", getCacheKey());
    return null;
  }

}