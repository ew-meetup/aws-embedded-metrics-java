/*
 *   Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License").
 *   You may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package software.amazon.cloudwatchlogs.emf.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.*;
import java.util.stream.Collectors;
import lombok.*;

/** Represents the MetricDirective part of the EMF schema. */
@AllArgsConstructor
class MetricDirective {
    @Setter
    @Getter
    @JsonProperty("Namespace")
    private String namespace;

    @JsonIgnore @Setter @Getter @With private Map<String, MetricDefinition> metrics;

    @JsonIgnore
    @Getter(AccessLevel.PROTECTED)
    private List<DimensionSet> dimensions;

    @JsonIgnore
    @Setter
    @Getter(AccessLevel.PROTECTED)
    private DimensionSet defaultDimensions;

    private boolean shouldUseDefaultDimension;

    MetricDirective() {
        namespace = "aws-embedded-metrics";
        metrics = new HashMap<>();
        dimensions = new ArrayList<>();
        defaultDimensions = new DimensionSet();
        shouldUseDefaultDimension = true;
    }

    void putDimensionSet(DimensionSet dimensionSet) {
        dimensions.add(dimensionSet);
    }

    void putMetric(String key, double value) {
        putMetric(key, value, Unit.NONE);
    }

    void putMetric(String key, double value, Unit unit) {
        if (metrics.containsKey(key)) {
            metrics.get(key).addValue(value);
        } else {
            metrics.put(key, new MetricDefinition(key, unit, value));
        }
    }

    @JsonProperty("Metrics")
    Collection<MetricDefinition> getAllMetrics() {
        return metrics.values();
    }

    @JsonProperty("Dimensions")
    List<Set<String>> getAllDimensionKeys() {
        return getAllDimensions().stream()
                .map(DimensionSet::getDimensionKeys)
                .collect(Collectors.toList());
    }

    /**
     * Override all existing dimensions.
     *
     * @param dimensionSets
     */
    void setDimensions(List<DimensionSet> dimensionSets) {
        shouldUseDefaultDimension = false;
        dimensions = dimensionSets;
    }

    /**
     * Return all the dimension sets. If there's a default dimension set, the custom dimensions are
     * prepended with the default dimensions.
     */
    List<DimensionSet> getAllDimensions() {
        if (!shouldUseDefaultDimension) {
            return dimensions;
        }

        if (dimensions.isEmpty()) {
            return Arrays.asList(defaultDimensions);
        }

        return dimensions.stream()
                .map(dim -> defaultDimensions.add(dim))
                .collect(Collectors.toList());
    }

    /**
     * Test if there's any metric added.
     *
     * @return true if no metrics have been added, otherwise, false
     */
    boolean hasNoMetrics() {
        return this.getMetrics().isEmpty();
    }
}
