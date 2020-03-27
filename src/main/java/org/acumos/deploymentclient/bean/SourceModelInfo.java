/*-
 * ===============LICENSE_START=======================================================
 * Acumos
 * ===================================================================================
 * Copyright (C) 2020 Nordix Foundation
 * ===================================================================================
 * This Acumos software file is distributed by Nordix Foundation
 * under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * This file is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ===============LICENSE_END=========================================================
 */

package org.acumos.deploymentclient.bean;

import java.io.Serializable;

/**
 * SourceModelInfo holds onto information about a source model such as its docker
 * image tag, capabilities, and model's metadata.
 */

public class SourceModelInfo implements Serializable {

  private static final long serialVersionUID = 1L;

  private String imageTag;
  private MLModel modelMetadata;
  private boolean continuousTrainingEnabled;

  public String getImageTag() {
    return imageTag;
  }

  public boolean isContinuousTrainingEnabled() {
    return continuousTrainingEnabled;
  }

  public void setContinuousTrainingEnabled(boolean continuousTrainingEnabled) {
    this.continuousTrainingEnabled = continuousTrainingEnabled;
  }

  public MLModel getModelMetadata() {
    return modelMetadata;
  }

  public void setModelMetadata(MLModel model) {
    this.modelMetadata = model;
  }

  public void setImageTag(String imageTag) {
    this.imageTag = imageTag;
  }



}
