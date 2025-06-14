# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).
{%- for release in releases %}

## {{ release.getTitle() }}
{%- for sectionName in release.getSectionNames() %}

### {{sectionName}}
{% for commit in release.getCommits(sectionName) %}
- {{ commit -}}
{%- endfor -%}
{%- endfor -%}
{%- endfor %}

{%- for version in versions %}
{{- version -}}
{%- endfor %}
