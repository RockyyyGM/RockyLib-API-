package dev.rocky.rockylib.api.event.v1;

import dev.rocky.rockylib.event.toposort.NodeSorting;
import net.minecraft.resources.ResourceLocation;

import java.lang.reflect.Array;
import java.util.*;
import java.util.function.Function;

public class ArrayBackedEvent<T> extends Event<T> {
    private final Function<T[], T> invokerFactory;
    private final Object lock = new Object();
    /**
     * Registered event phases.
     */
    private final Map<ResourceLocation, EventPhaseData<T>> phases = new LinkedHashMap<>();
    /**
     * Phases sorted in the correct dependency order.
     */
    private final List<EventPhaseData<T>> sortedPhases = new ArrayList<>();
    private T[] handlers;

    @SuppressWarnings("unchecked")
    public ArrayBackedEvent(Class<? super T> type, Function<T[], T> invokerFactory) {
        this.invokerFactory = invokerFactory;
        this.handlers = (T[]) Array.newInstance(type, 0);
        update();
    }

    public void update() {
        this.invoker = invokerFactory.apply(handlers);
    }

    @Override
    public void register(T listener) {
        register(DEFAULT_PHASE, listener);
    }

    @Override
    public void register(ResourceLocation phaseResourceLocation, T listener) {
        Objects.requireNonNull(phaseResourceLocation, "Tried to register a listener for a null phase!");
        Objects.requireNonNull(listener, "Tried to register a null listener!");

        synchronized (lock) {
            getOrCreatePhase(phaseResourceLocation, true).addListener(listener);
            rebuildInvoker(handlers.length + 1);
        }
    }

    private EventPhaseData<T> getOrCreatePhase(ResourceLocation id, boolean sortIfCreate) {
        EventPhaseData<T> phase = phases.get(id);

        if (phase == null) {
            phase = new EventPhaseData<>(id, handlers.getClass().getComponentType());
            phases.put(id, phase);
            sortedPhases.add(phase);

            if (sortIfCreate) {
                NodeSorting.sort(sortedPhases, "event phases", Comparator.comparing(data -> data.id));
            }
        }

        return phase;
    }

    private void rebuildInvoker(int newLength) {
        // Rebuild handlers.
        if (sortedPhases.size() == 1) {
            // Special case with a single phase: use the array of the phase directly.
            handlers = sortedPhases.get(0).listeners;
        } else {
            @SuppressWarnings("unchecked") T[] newHandlers =
                    (T[]) Array.newInstance(handlers.getClass().getComponentType(), newLength);
            int newHandlersIndex = 0;

            for (EventPhaseData<T> existingPhase : sortedPhases) {
                int length = existingPhase.listeners.length;
                System.arraycopy(existingPhase.listeners, 0, newHandlers, newHandlersIndex, length);
                newHandlersIndex += length;
            }

            handlers = newHandlers;
        }

        // Rebuild invoker.
        update();
    }

    @Override
    public void addPhaseOrdering(ResourceLocation firstPhase, ResourceLocation secondPhase) {
        Objects.requireNonNull(firstPhase, "Tried to add an ordering for a null phase.");
        Objects.requireNonNull(secondPhase, "Tried to add an ordering for a null phase.");
        if (firstPhase.equals(secondPhase))
            throw new IllegalArgumentException("Tried to add a phase that depends on itself.");

        synchronized (lock) {
            EventPhaseData<T> first = getOrCreatePhase(firstPhase, false);
            EventPhaseData<T> second = getOrCreatePhase(secondPhase, false);
            EventPhaseData.link(first, second);
            NodeSorting.sort(this.sortedPhases, "event phases", Comparator.comparing(data -> data.id));
            rebuildInvoker(handlers.length);
        }
    }
}
