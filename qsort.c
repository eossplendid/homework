#include <stdio.h>
#include <unistd.h>
#include <string.h>
#include <stdlib.h>

int compare(const void *p1, const void *p2)
{
	int ret = 0;
	printf("in compare:\np1:%p\np2:%p\n",&p1, &p2);
	ret = strcmp((char *)p1, (char *)p2);

	return ret;
}

int mysort(char *p[20], int len) {
	int i,j;
	char *tmp;
	for (i = 0; i < len; i++) 
		for (j = 0;i + j < len -1; j++) {
			if (strcmp(p[j], p[j+1]) > 0) {
				tmp = p[j];
				p[j] = p[j+1];
				p[j+1] = tmp;	
			}
		}
	for (i = 0; i < len; i++){
		printf("%d:%s\n", i, p[i]);
	}

}

int mysort2(char *p[20], int len) {
	int i,j;
	char *tmp;
	for (i = 0; i < len-1; i++)
		for (j = i + 1; j < len; j++) {
			if (strcmp(p[i], p[j]) > 0) {
				tmp = p[i];
				p[i] = p[j];
				p[j] = tmp;
			}
		}
	return 0;
}

int cmp(const void *p1, const void *p2)
{
	char **t1 = (char **)p1;
	char **t2 = (char **)p2;
	return strcmp(*t1, *t2);
}
int test()
{
	char pList[4][60] = {"test-udid01", "12182318317", "bkakhfkshfka", "8871236sjaffh"};
	char *p[4];
	int i;

#if 0
	pList[0] = "test-udid01";
	pList[1] = "1391389712897";
	pList[2] = "bkdjkfjsakfhsjklgjhsfah";
	pList[3] = "8312dwljqwjkhsajk";

#endif

	qsort(pList, 4, sizeof(pList[0]), compare);
	printf("sizeof s[0]:%d\n", sizeof(pList[0]));
	printf("sizeof p[0]:%d\n", sizeof(p[0]));

	for (i = 0; i < 4; i++) {
		printf("%d:%s\n", i, pList[i]);
	}

	for (i = 0; i < 4; i++) {
		p[i] = (char *)malloc(60);
		printf("malloc p:%p\n", p[i]);
	}
	strcpy(p[0], "test-udid01");
	strcpy(p[1], "12313181312313321");
	strcpy(p[2], "bdhkjfkhklgjafhasfhfkjsa");
	strcpy(p[3], "8781fdjsakfjl32123");
	qsort(p, 4, sizeof(p[0]), cmp);
//	mysort2(p, 4);
	printf("wonderful!!!!!!!!!!!!!1\n");
	for (i = 0; i < 4; i++) {
		printf("%d:%s\n", i, p[i]);
		free(p[i]);
	}
	return 0;
}

int main()
{
	FILE *fp;
	char buffer[80];
	fp = popen("./test.sh", "r");
#if 0
	fgets(buffer, sizeof(buffer), fp);
	printf("%s\n", buffer);
#endif
	pclose(fp);
	test();
	//execl("./test.sh",NULL);
	return 0;
}
