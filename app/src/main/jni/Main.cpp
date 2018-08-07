//Created By Xs.JIONG at 2018.7.5
//JZM has already cleared all the bug will appeared
//And also, no compiling problems
#include <jni.h>
#include <Substrate.h>
#include <fstream>
#include <stdlib.h>
#include <Common.h>

std::ofstream out;
const char *MCOptionO="/data/data/com.mojang.minecraftpe/games/com.mojang/minecraftpe/options.txt";
const char *MCOptionR="/sdcard/games/com.mojang/minecraftpe/options.txt";
const char *GamesDirO="/data/data/com.mojang.minecraftpe/games/com.mojang/minecraftpe/";
const char *GamesDirR="/storage/emulated/0/games";
char *MCReadOption="/data/data/com.mojang.minecraftpe/games/com.mojang/minecraftpe//options.txt";
int MCROPTSize;
int MCOPTOSize;
int GDOSize,GDRSize;
char __TMP[1024];
bool First=1;
namespace JNIExport {
	void setTmpOptionFilePath(JNIEnv* env, jobject jthis, jstring str) {
		MCOptionR=env->GetStringUTFChars(str,NULL);
	}
	jstring getTmpOptionFilePath(JNIEnv* env, jobject jthis) {
		return env->NewStringUTF(MCOptionR);
	}
	void setGamesDir(JNIEnv* env, jobject jthis, jstring str) {
		GamesDirR=env->GetStringUTFChars(str,NULL);
		GDRSize=strlen(GamesDirR);
	}
	jstring getGamesDir(JNIEnv* env, jobject jthis) {
		return env->NewStringUTF(GamesDirR);
	}
	static JNINativeMethod ALL[]={
		{"setTmpOptionFilePath","(Ljava/lang/String;)V",(void*)setTmpOptionFilePath},{"getTmpOptionFilePath","()Ljava/lang/String;",(void*)getTmpOptionFilePath},{"setGamesDir","(Ljava/lang/String;)V",(void*)setGamesDir},{"getGamesDir","()Ljava/lang/String;",(void*)getGamesDir}

	};
};
static bool StringCmp(const char *a, const char *b, int size) {
	for (int i=0;i<size;i++) if (a[i]!=b[i]) return 0;
	return 1;
}
const char* convert(const char *s) {
	if (StringCmp(s,MCOptionO,MCOPTOSize)) {
		out<<"Refuse ";
		return MCOptionR;
	}
	if (StringCmp(s,GamesDirO,GDOSize)) {
		int len=strlen(s);
		memcpy(__TMP,GamesDirR,GDRSize);
		memcpy(__TMP+GDRSize,s+GDOSize,len-GDOSize+1);
		len=GDRSize+len-GDOSize;
		bool lastFit=0;
		int res=-1;
		for (int i=len-1;i>=0;i--) {
			if (__TMP[i]=='/')
				if (lastFit) {res=i;break;} else lastFit=1;
			else lastFit=0;
		}
		if (res!=-1)
			memcpy(__TMP+res,__TMP+res+1,len-res);
		return __TMP;
	}
	return s;
}
FILE* (*OriginOpen)(const char *filename,const char *mode);
FILE* ReplacedOpen(const char *filename, const char *mode) {
	if (filename!=NULL&&filename[0]!='/') return NULL;
	const char *q=convert(filename);
	out<<q<<' '<<mode<<' ';
	FILE* r=(q==NULL?NULL:OriginOpen(q, mode));
	out<<(r==NULL?'X':'O')<<'\n';
	out.flush();
	return r;
}
bool RegisterNativeMethods(JNIEnv* env) {
	jclass clz=env->FindClass("com/jxs/vblock/MinecraftManager");
	if (clz==NULL) return 0;
	if (env->RegisterNatives(clz, JNIExport::ALL, sizeof(JNIExport::ALL)/sizeof(JNIExport::ALL[0]))<0)
		return 0;
	return 1;
}
void* MC;
std::string GetGameVersionStringHooked(void* a) {
	out<<"GetGameVersion Called\n";
	out.flush();
	return "JIOSJADIOD";
}
inline void HookFunctions() {
	out<<"MC Address:"<<(int)MC<<'\n';
	void* addr=dlsym(MC, "_ZN6Common20getGameVersionStringEv");
	out<<"Address:"<<(int)addr<<'\n';
	out.flush();
	MSHookFunction(addr,(void*)&GetGameVersionStringHooked,NULL);
}
extern "C" JNIEXPORT jint JNI_OnLoad(JavaVM* vm, void* reserved) {
	out.open("/sdcard/VIDENativeLog.txt");
	MCOPTOSize=strlen(MCOptionO),MCROPTSize=strlen(MCReadOption);
	GDOSize=strlen(GamesDirO),GDRSize=strlen(GamesDirR);
	JNIEnv* env=NULL;
	if (vm->GetEnv((void**)&env, JNI_VERSION_1_6) != JNI_OK) return JNI_ERR;
	if (env==NULL) return JNI_ERR;
	if (!RegisterNativeMethods(env)) return JNI_ERR;
	out<<"Registered JNI\n";
	MSImageRef Libc=MSGetImageByName("/system/lib/libc.so");
	MSHookFunction(MSFindSymbol(Libc,"fopen"),(void*)&ReplacedOpen,(void**)&OriginOpen);
	out<<"Hooked\n";
	out.flush();
	MC=dlopen("/data/data/com.mojang.minecraftpe/lib/libminecraftpe.so",RTLD_LAZY);
	HookFunctions();
	return JNI_VERSION_1_6;
}