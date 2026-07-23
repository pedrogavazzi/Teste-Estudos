package com.pedrogavazzi.controleestudos.data

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun tipoAlertaParaString(tipo: TipoAlerta): String = tipo.name

    @TypeConverter
    fun stringParaTipoAlerta(valor: String): TipoAlerta = when (valor) {
        TipoAlerta.SEM_SOM.name -> TipoAlerta.SEM_SOM
        // Valores antigos de uma versão anterior (com vibração separada) caem em "com som".
        else -> TipoAlerta.COM_SOM
    }
}
