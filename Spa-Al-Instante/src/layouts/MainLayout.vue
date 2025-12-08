<template>
  <q-layout view="lHh Lpr lFf">
    <!-- HEADER -->
    <q-header elevated class="bg-primary text-white">
      <q-toolbar>
        <q-toolbar-title class="text-weight-bold"> Spa al Instante </q-toolbar-title>

        <div class="gt-sm row items-center q-gutter-x-md">
          <q-btn flat label="Inicio" to="/" />
          <q-btn flat label="Servicios" href="#servicios" />
          <q-btn flat label="Paquetes" href="#paquetes" />
          <q-btn flat label="Nosotros" href="#nosotros" />
          <q-btn flat label="Contacto" href="#contacto" />
        </div>

        <q-btn
          class="q-ml-md bg-secondary text-dark rounded-borders"
          label="Reservar"
          href="#contacto"
        />
      </q-toolbar>
    </q-header>

    <q-page-container>
      <router-view />
    </q-page-container>
  </q-layout>
</template>

<script setup></script>
