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
package io.github.drrb;

import com.thoughtworks.go.plugin.api.logging.Logger;
import com.thoughtworks.go.plugin.api.material.packagerepository.PackageConfiguration;
import com.thoughtworks.go.plugin.api.material.packagerepository.PackageMaterialPoller;
import com.thoughtworks.go.plugin.api.material.packagerepository.PackageRevision;
import com.thoughtworks.go.plugin.api.material.packagerepository.RepositoryConfiguration;
import com.thoughtworks.go.plugin.api.response.Result;
import io.github.drrb.forge.Forge;
import io.github.drrb.forge.ModuleRelease;
import io.github.drrb.forge.ModuleSpec;
import io.github.drrb.util.Exceptions;

public class ForgePoller implements PackageMaterialPoller {

    private static final Logger LOGGER = Logger.getLoggerFor(ForgePoller.class);
    private final Forge.Factory forgeFactory;

    public ForgePoller(Forge.Factory forgeFactory) {
        this.forgeFactory = forgeFactory;
    }

    @Override
    public Result checkConnectionToRepository(RepositoryConfiguration repositoryConfiguration) {
        Forge forge = forgeFactory.build(repositoryConfiguration);
        try {
            forge.ping();
            return new Result().withSuccessMessages("Connection successful");
        } catch (Forge.PingFailure pingFailure) {
            return new Result().withErrorMessages(Exceptions.render(pingFailure));
        }
    }

    @Override
    public Result checkConnectionToPackage(PackageConfiguration packageConfiguration, RepositoryConfiguration repositoryConfiguration) {
        ModuleSpec moduleSpec = ModuleSpec.from(packageConfiguration);
        Forge forge = forgeFactory.build(repositoryConfiguration);

        try {
            forge.ping(moduleSpec);
            return new Result().withSuccessMessages("Found " + moduleSpec.getName());
        } catch (Forge.PingFailure pingFailure) {
            return new Result().withErrorMessages(Exceptions.render(pingFailure));
        }
    }

    @Override
    public PackageRevision getLatestRevision(PackageConfiguration packageConfiguration, RepositoryConfiguration repositoryConfiguration) {
        ModuleSpec module = ModuleSpec.from(packageConfiguration);
        Forge forge = forgeFactory.build(repositoryConfiguration);
        log("Looking up latest revision of module %s in forge %s", module, forge);

        try {
            ModuleRelease latestRelease = forge.getLatestVersion(module);
            return latestRelease.toPackageRevision();
        } catch (Forge.ModuleNotFound moduleNotFound) {
            log("Module %s not found in forge %s: %s", module, forge, Exceptions.render(moduleNotFound));
            return null;
        }
    }

    @Override
    public PackageRevision latestModificationSince(PackageConfiguration packageConfiguration, RepositoryConfiguration repositoryConfiguration, PackageRevision lastKnownRevision) {
        ModuleSpec module = ModuleSpec.from(packageConfiguration);
        Forge forge = forgeFactory.build(repositoryConfiguration);
        log("Looking up latest revision of module %s in forge %s since version %s", module, forge, lastKnownRevision.getRevision());

        try {
            ModuleRelease latestRelease = forge.getLatestVersion(module);
            //TODO: warn if this release is earlier than lastKnownRevision
            return latestRelease.toPackageRevision();
        } catch (Forge.ModuleNotFound moduleNotFound) {
            log("Module %s not found in forge %s: %s", module, forge, Exceptions.render(moduleNotFound));
            return null;
        }
    }

    protected void log(String message, Object... args) {
        for (int i = 0; i < args.length; i++) {
            if (args[i] instanceof Throwable) {
                args[i] = Exceptions.render((Throwable) args[i]);
            }
        }
        LOGGER.info(String.format(message, args));
    }
}
