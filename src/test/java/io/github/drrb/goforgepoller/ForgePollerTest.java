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

import com.thoughtworks.go.plugin.api.material.packagerepository.PackageConfiguration;
import com.thoughtworks.go.plugin.api.material.packagerepository.PackageRevision;
import com.thoughtworks.go.plugin.api.material.packagerepository.RepositoryConfiguration;
import com.thoughtworks.go.plugin.api.response.Result;
import io.github.drrb.goforgepoller.forge.Forge;
import io.github.drrb.goforgepoller.forge.ModuleSpec;
import io.github.drrb.goforgepoller.forge.ModuleVersion;
import io.github.drrb.goforgepoller.forge.Version;
import io.github.drrb.goforgepoller.util.Log;
import io.github.drrb.test.NoLogging;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.net.URL;

import static io.github.drrb.goforgepoller.forge.Forge.PingFailure;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ForgePollerTest {

    @Rule
    public final NoLogging noLogging = NoLogging.rule();

    private ForgePoller poller;

    @Mock
    private Forge.Factory forgeFactory;
    @Mock
    private Forge forge;
    @Mock
    private ModuleSpec.Factory moduleSpecFactory;
    @Mock
    private ModuleSpec moduleSpec;
    private RepositoryConfiguration repoConfig;
    private PackageConfiguration packageConfig;
    private ModuleVersion moduleVersion;

    @Before
    public void setUp() throws Exception {
        poller = new ForgePoller(forgeFactory, moduleSpecFactory);

        repoConfig = new RepositoryConfiguration();
        packageConfig = new PackageConfiguration();
        moduleVersion = new ModuleVersion("", Version.of("1.0.0"), new URL("http://forge.example.com/puppetlabs-apache-1.0.0.tar.gz"));

        when(forgeFactory.build(repoConfig)).thenReturn(forge);
        when(moduleSpecFactory.build(packageConfig)).thenReturn(moduleSpec);
    }

    @Test
    public void shouldReturnSuccessWhenForgeConnectionSucceeds() throws Exception {
        Result result = poller.checkConnectionToRepository(repoConfig);

        assertThat(result.isSuccessful(), is(true));
    }

    @Test
    public void shouldReturnFailureWhenForgeConnectionFails() throws Exception {
        doThrow(new PingFailure("Connection refused")).when(forge).ping();

        Result result = poller.checkConnectionToRepository(repoConfig);

        assertThat(result.isSuccessful(), is(false));
        assertThat(result.getMessages(), hasItem(containsString("Connection refused")));
    }

    @Test
    public void shouldReturnSuccessWhenPackageConnectionSucceeds() throws Exception {
        Result result = poller.checkConnectionToPackage(packageConfig, repoConfig);

        assertThat(result.isSuccessful(), is(true));
    }

    @Test
    public void shouldReturnFailureWhenPackageConnectionFails() throws Exception {
        doThrow(new PingFailure("Not found")).when(forge).ping(moduleSpec);

        Result result = poller.checkConnectionToPackage(packageConfig, repoConfig);

        assertThat(result.isSuccessful(), is(false));
        assertThat(result.getMessages(), hasItem(containsString("Not found")));
    }

    @Test
    public void shouldReturnLatestVersionOfPackage() throws Exception {
        when(forge.getLatestVersion(moduleSpec)).thenReturn(moduleVersion);

        PackageRevision result = poller.getLatestRevision(packageConfig, repoConfig);

        assertThat(result.getRevision(), is("1.0.0"));
    }

    @Test
    public void shouldReturnNullWhenPackageNotFound() throws Exception {
        when(forge.getLatestVersion(moduleSpec)).thenThrow(new Forge.ModuleNotFound("Failed to list versions of module 'puppetlabs/apache' (500)"));

        PackageRevision result = poller.getLatestRevision(packageConfig, repoConfig);

        assertThat(result, is(nullValue()));
    }

    @Test
    public void shouldReturnLatestVersionOfPackageWhenReturningLatestModification() throws Exception {
        PackageRevision lastKnownRevision = new PackageRevision("0.9.0", null, null);
        when(forge.getLatestVersion(moduleSpec)).thenReturn(moduleVersion);

        PackageRevision result = poller.latestModificationSince(packageConfig, repoConfig, lastKnownRevision);

        assertThat(result.getRevision(), is("1.0.0"));
    }

    @Test
    public void shouldReturnNullIfLatestVersionIsntGreaterThanLastKnownVersion() throws Exception {
        PackageRevision lastKnownRevision = new PackageRevision("1.0.0", null, null);
        when(forge.getLatestVersion(moduleSpec)).thenReturn(moduleVersion);

        PackageRevision result = poller.latestModificationSince(packageConfig, repoConfig, lastKnownRevision);

        assertThat(result, is(nullValue()));
    }

    @Test
    public void shouldReturnNullIfLatestVersionNotFound() throws Exception {
        PackageRevision lastKnownRevision = new PackageRevision("1.0.0", null, null);
        when(forge.getLatestVersion(moduleSpec)).thenThrow(new Forge.ModuleNotFound("Failed to list versions of module 'puppetlabs/apache' (500)"));

        PackageRevision result = poller.latestModificationSince(packageConfig, repoConfig, lastKnownRevision);

        assertThat(result, is(nullValue()));
    }
}
