{{/*
myRC Helm Chart - Template Helpers
*/}}

{{/*
Expand the name of the chart.
*/}}
{{- define "myrc.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Create a fully qualified app name.
*/}}
{{- define "myrc.fullname" -}}
{{- if .Values.fullnameOverride }}
{{- .Values.fullnameOverride | trunc 63 | trimSuffix "-" }}
{{- else }}
{{- $name := default .Chart.Name .Values.nameOverride }}
{{- if contains $name .Release.Name }}
{{- .Release.Name | trunc 63 | trimSuffix "-" }}
{{- else }}
{{- printf "%s-%s" .Release.Name $name | trunc 63 | trimSuffix "-" }}
{{- end }}
{{- end }}
{{- end }}

{{/*
Chart label
*/}}
{{- define "myrc.chart" -}}
{{- printf "%s-%s" .Chart.Name .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Common labels
*/}}
{{- define "myrc.labels" -}}
helm.sh/chart: {{ include "myrc.chart" . }}
{{ include "myrc.selectorLabels" . }}
app.kubernetes.io/version: {{ .Chart.AppVersion | quote }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{- if .Values.global.labels }}
{{ toYaml .Values.global.labels }}
{{- end }}
{{- end }}

{{/*
Selector labels
*/}}
{{- define "myrc.selectorLabels" -}}
app.kubernetes.io/name: {{ include "myrc.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end }}

{{/*
Backend selector labels
*/}}
{{- define "myrc.backend.selectorLabels" -}}
{{ include "myrc.selectorLabels" . }}
app.kubernetes.io/component: backend
{{- end }}

{{/*
Frontend selector labels
*/}}
{{- define "myrc.frontend.selectorLabels" -}}
{{ include "myrc.selectorLabels" . }}
app.kubernetes.io/component: frontend
{{- end }}

{{/*
Database selector labels
*/}}
{{- define "myrc.database.selectorLabels" -}}
{{ include "myrc.selectorLabels" . }}
app.kubernetes.io/component: database
{{- end }}

{{/*
Service account name
*/}}
{{- define "myrc.serviceAccountName" -}}
{{- if .Values.serviceAccount.create }}
{{- default (include "myrc.fullname" .) .Values.serviceAccount.name }}
{{- else }}
{{- default "default" .Values.serviceAccount.name }}
{{- end }}
{{- end }}

{{/*
Database host — internal StatefulSet or external
*/}}
{{- define "myrc.databaseHost" -}}
{{- if .Values.postgresql.enabled }}
{{- printf "%s-postgres" (include "myrc.fullname" .) }}
{{- else }}
{{- .Values.externalDatabase.host }}
{{- end }}
{{- end }}

{{/*
Database port
*/}}
{{- define "myrc.databasePort" -}}
{{- if .Values.postgresql.enabled }}
5432
{{- else }}
{{- .Values.externalDatabase.port | default 5432 }}
{{- end }}
{{- end }}

{{/*
Database name
*/}}
{{- define "myrc.databaseName" -}}
{{- if .Values.postgresql.enabled }}
{{- .Values.postgresql.auth.database }}
{{- else }}
{{- .Values.externalDatabase.database }}
{{- end }}
{{- end }}

{{/*
JDBC URL
*/}}
{{- define "myrc.jdbcUrl" -}}
jdbc:postgresql://{{ include "myrc.databaseHost" . }}:{{ include "myrc.databasePort" . }}/{{ include "myrc.databaseName" . }}
{{- end }}

{{/*
Database secret name
*/}}
{{- define "myrc.databaseSecretName" -}}
{{- if and (not .Values.postgresql.enabled) .Values.externalDatabase.existingSecret }}
{{- .Values.externalDatabase.existingSecret }}
{{- else }}
{{- printf "%s-db-secret" (include "myrc.fullname" .) }}
{{- end }}
{{- end }}

{{/*
LDAP URL — test LDAP or external
*/}}
{{- define "myrc.ldapUrl" -}}
{{- if .Values.testLdap.enabled }}
{{- printf "ldap://%s-testldap:%d" (include "myrc.fullname" .) (.Values.testLdap.service.ldapPort | int) }}
{{- else }}
{{- .Values.auth.ldap.url }}
{{- end }}
{{- end }}

{{/*
Namespace
*/}}
{{- define "myrc.namespace" -}}
{{- default .Release.Namespace .Values.global.namespace }}
{{- end }}
