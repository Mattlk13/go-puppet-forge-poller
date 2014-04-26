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
package io.github.drrb.goforgepoller;

import com.thoughtworks.go.plugin.api.config.Property;
import com.thoughtworks.go.plugin.api.material.packagerepository.PackageConfiguration;
import com.thoughtworks.go.plugin.api.material.packagerepository.RepositoryConfiguration;
import com.thoughtworks.go.plugin.api.response.validation.ValidationResult;
import org.junit.Before;
import org.junit.Test;

import static com.thoughtworks.go.plugin.api.config.Property.DISPLAY_NAME;
import static io.github.drrb.goforgepoller.ForgePollerPluginConfig.*;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

public class ForgePollerPluginConfigTest {

    private ForgePollerPluginConfig config;

    @Before
    public void setUp() throws Exception {
        config = new ForgePollerPluginConfig();
    }

    @Test
    public void shouldConstructRepoConfig() throws Exception {
        RepositoryConfiguration repoConfig = config.getRepositoryConfiguration();
        assertThat(repoConfig.get(FORGE_URL).getOption(DISPLAY_NAME), is("Forge URL"));
    }

    @Test
    public void shouldConstructPackageConfig() throws Exception {
        PackageConfiguration packageConfig = config.getPackageConfiguration();
        assertThat(packageConfig.get(MODULE_NAME).getOption(DISPLAY_NAME), is("Module Name"));
    }

    @Test
    public void shouldAcceptRepoConfigIfForgeUrlSpecified() throws Exception {
        RepositoryConfiguration repoConfig = new RepositoryConfiguration();
        repoConfig.add(new Property(FORGE_URL).withDefault("http://forge.puppetlabs.com"));

        ValidationResult validationResult = config.isRepositoryConfigurationValid(repoConfig);

        assertThat(validationResult.isSuccessful(), is(true));
    }

    @Test
    public void shouldRejectRepoConfigIfUrlIsEmpty() throws Exception {
        RepositoryConfiguration repoConfig = new RepositoryConfiguration();
        repoConfig.add(new Property(FORGE_URL).withDefault("  "));

        ValidationResult validationResult = config.isRepositoryConfigurationValid(repoConfig);

        assertThat(validationResult.isSuccessful(), is(false));
        assertThat(validationResult.getMessages(), hasItem(equalTo("Forge URL is mandatory")));
    }

    @Test
    public void shouldRejectRepoConfigIfUrlIsNull() throws Exception {
        RepositoryConfiguration repoConfig = new RepositoryConfiguration();
        repoConfig.add(new Property(FORGE_URL).withDefault(null));

        ValidationResult validationResult = config.isRepositoryConfigurationValid(repoConfig);

        assertThat(validationResult.isSuccessful(), is(false));
        assertThat(validationResult.getMessages(), hasItem(equalTo("Forge URL is mandatory")));
    }

    @Test
    public void shouldAcceptRepoConfigIfForgeUrlIsInvalid() throws Exception {
        RepositoryConfiguration repoConfig = new RepositoryConfiguration();
        repoConfig.add(new Property(FORGE_URL).withDefault("x"));

        ValidationResult validationResult = config.isRepositoryConfigurationValid(repoConfig);

        assertThat(validationResult.isSuccessful(), is(false));
        assertThat(validationResult.getMessages(), hasItem(equalTo("Forge URL must be a URL")));
    }

    @Test
    public void shouldAcceptRepoConfigIfForgeUrlIsNonHttp() throws Exception {
        RepositoryConfiguration repoConfig = new RepositoryConfiguration();
        repoConfig.add(new Property(FORGE_URL).withDefault("ftp://example.com"));

        ValidationResult validationResult = config.isRepositoryConfigurationValid(repoConfig);

        assertThat(validationResult.isSuccessful(), is(false));
        assertThat(validationResult.getMessages(), hasItem(equalTo("Forge URL must be an HTTP(S) URL")));
    }

    @Test
    public void shouldAcceptPackageConfigIfNoVersionsSpecified() throws Exception {
        PackageConfiguration packageConfig = new PackageConfiguration();
        packageConfig.add(new Property(MODULE_NAME).withDefault("puppetlabs/apache"));

        ValidationResult validationResult = config.isPackageConfigurationValid(packageConfig, new RepositoryConfiguration());

        assertThat(validationResult.isSuccessful(), is(true));
    }

    @Test
    public void shouldRejectPackageConfigIfModuleNameIsEmpty() throws Exception {
        PackageConfiguration packageConfig = new PackageConfiguration();
        packageConfig.add(new Property(MODULE_NAME).withDefault("  "));

        ValidationResult validationResult = config.isPackageConfigurationValid(packageConfig, new RepositoryConfiguration());

        assertThat(validationResult.isSuccessful(), is(false));
        assertThat(validationResult.getMessages(), hasItem(equalTo("Module name is mandatory")));
    }

    @Test
    public void shouldRejectPackageConfigIfModuleNameIsNull() throws Exception {
        PackageConfiguration packageConfig = new PackageConfiguration();
        packageConfig.add(new Property(MODULE_NAME).withDefault(null));

        ValidationResult validationResult = config.isPackageConfigurationValid(packageConfig, new RepositoryConfiguration());

        assertThat(validationResult.isSuccessful(), is(false));
        assertThat(validationResult.getMessages(), hasItem(equalTo("Module name is mandatory")));
    }

    @Test
    public void shouldRejectPackageConfigIfModuleNameInWrongFormat() throws Exception {
        PackageConfiguration packageConfig = new PackageConfiguration();
        packageConfig.add(new Property(MODULE_NAME).withDefault("apache"));

        ValidationResult validationResult = config.isPackageConfigurationValid(packageConfig, new RepositoryConfiguration());

        assertThat(validationResult.isSuccessful(), is(false));
        assertThat(validationResult.getMessages(), hasItem(equalTo("Module name should be in format \"author/module\"")));
    }

    @Test
    public void shouldRejectPackageConfigIfLowerVersionBoundInWrongFormat() throws Exception {
        PackageConfiguration packageConfig = new PackageConfiguration();
        packageConfig.add(new Property(MODULE_NAME).withDefault("puppetlabs/apache"));
        packageConfig.add(new Property(LOWER_VERSION_BOUND_INCLUSIVE).withDefault("XX Bad format XX"));

        ValidationResult validationResult = config.isPackageConfigurationValid(packageConfig, new RepositoryConfiguration());

        assertThat(validationResult.isSuccessful(), is(false));
        assertThat(validationResult.getMessages(), hasItem(equalTo("Version to poll >= should be a version number")));
    }

    @Test
    public void shouldRejectPackageConfigIfUpperVersionBoundInWrongFormat() throws Exception {
        PackageConfiguration packageConfig = new PackageConfiguration();
        packageConfig.add(new Property(MODULE_NAME).withDefault("puppetlabs/apache"));
        packageConfig.add(new Property(UPPER_VERSION_BOUND_EXCLUSIVE).withDefault("XX Bad format XX"));

        ValidationResult validationResult = config.isPackageConfigurationValid(packageConfig, new RepositoryConfiguration());

        assertThat(validationResult.isSuccessful(), is(false));
        assertThat(validationResult.getMessages(), hasItem(equalTo("Version to poll < should be a version number")));
    }
}
