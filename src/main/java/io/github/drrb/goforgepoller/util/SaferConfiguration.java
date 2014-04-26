/**
 * Go Forge Poller
 * Copyright (C) 2014 drrb
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Go Forge Poller. If not, see <http://www.gnu.org/licenses/>.
 */
package io.github.drrb.goforgepoller.util;

import com.thoughtworks.go.plugin.api.material.packagerepository.Configuration;
import com.thoughtworks.go.plugin.api.material.packagerepository.Property;

import javax.annotation.Nonnull;

public class SaferConfiguration {

    private final Configuration delegate;

    public SaferConfiguration(Configuration delegate) {
        this.delegate = delegate;
    }

    @Nonnull
    public String get(String key) {
        Property property = delegate.get(key);
        return property == null || property.getValue() == null ? "" : property.getValue().trim();
    }
}
