package io.github.palexdev.enbmanager.model.games;

import io.github.palexdev.enbmanager.settings.base.GameSettings;
import io.github.palexdev.enbmanager.utils.OSUtils;
import oshi.software.os.OSProcess;
import oshi.software.os.OperatingSystem;

import java.util.List;
import java.util.Objects;

public abstract class GameBase<S extends GameSettings> implements Game {
    //================================================================================
    // Properties
    //================================================================================
    protected final OSUtils os;
    protected final String name;
    protected final String exeName;
    protected final S settings;

    //================================================================================
    // Constructors
    //================================================================================
    public GameBase(OSUtils os, S settings, String name, String exeName) {
        this.os = os;
        this.name = name;
        this.exeName = exeName;
        this.settings = settings;
    }

    //================================================================================
    // Overridden Methods
    //================================================================================
    @Override
    public String name() {
        return name;
    }

    @Override
    public String exeName() {
        return exeName;
    }

    @Override
    public boolean isRunning() {
        try {
            OperatingSystem os = this.os.getOS();
            List<OSProcess> processes = os.getProcesses(
                p -> exeName().equals(p.getName()),
                OperatingSystem.ProcessSorting.NO_SORTING,
                0
            );
            return !processes.isEmpty();
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GameBase<?> other = (GameBase<?>) o;
        return Objects.equals(name, other.name) && Objects.equals(exeName, other.exeName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, exeName);
    }
}
